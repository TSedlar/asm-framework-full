package io.disassemble.asm.visitor.flow;

import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.util.Assembly;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.*;

/**
 * @author Tyler Sedlar
 * @since 4/7/2016
 */
public class BasicBlock {

    protected final List<BasicBlock> successors = new ArrayList<>();
    protected BasicBlock predecessor;

    public final String id;
    public final ClassMethod method;
    public final int start, end;

    private BasicInstruction[] instructions;
    private Integer[] instructionIndices;

    public BasicBlock(String id, ClassMethod method, int start, int end, List<Integer> instructionIndices) {
        this.id = id;
        this.method = method;
        this.start = start;
        this.end = end;
        this.instructionIndices = instructionIndices.toArray(new Integer[instructionIndices.size()]);
    }

    /**
     * Gets the list of successors for this BasicBlock.
     *
     * @return The list of sucessors for this BasicBlock.
     */
    public List<BasicBlock> successors() {
        return Collections.unmodifiableList(successors);
    }

    /**
     * Gets this block's predecessor.
     *
     * @return This block's predecessor.
     */
    public BasicBlock predecessor() {
        return predecessor;
    }

    public int size() {
        return instructionIndices.length;
    }

    /**
     * Gets the starting instruction of this block.
     *
     * @return The starting instruction of this block.
     */
    public BasicInstruction entry() {
        return instructions()[0];
    }

    /**
     * Gets the ending instruction of this block.
     *
     * @return The ending instruction of this block.
     */
    public BasicInstruction exit() {
        BasicInstruction[] insns = instructions();
        return insns[insns.length - 1];
    }

    /**
     * Gets a list of instructions within this block.
     *
     * @param cached whether to grab a cached list of instructions or not.
     * @return A list of instructions within this block.
     */
    public BasicInstruction[] instructions(boolean cached) {
        if (!cached || instructions == null) {
            instructions = new BasicInstruction[size()];
            for (int i = 0; i < instructions.length; i++) {
                instructions[i] = new BasicInstruction(this, method.instructions().get(instructionIndices[i]));
            }
        }
        return instructions;
    }

    /**
     * Gets a list of instructions within this block.
     *
     * @return A list of instructions within this block.
     */
    public BasicInstruction[] instructions() {
        return instructions(true);
    }

    /**
     * Gets the index of the given instruction.
     *
     * @param insn The instruction to get an index for.
     * @return The index of the given instruction.
     */
    public int indexOf(BasicInstruction insn) {
        return Arrays.asList(instructions()).indexOf(insn);
    }

    /**
     * Gets the instruction at the given index.
     *
     * @param index The index to get at.
     * @return The instruction at the given index.
     */
    public BasicInstruction get(int index) {
        return instructions()[index];
    }

    /**
     * Gets the path that branches to a true value.
     *
     * @return The path that branches to a true value.
     */
    public Optional<BasicBlock> trueBranch() {
        if (successors.size() == 1) {
            return Optional.of(successors.get(0));
        }
        BasicInstruction endInsn = exit();
        if (endInsn.insn instanceof JumpInsnNode) {
            LabelNode label = ((JumpInsnNode) endInsn.insn).label;
            for (BasicBlock successor : successors) {
                BasicInstruction startInsn = successor.entry();
                if (startInsn != null && label == startInsn.insn) {
                    return Optional.of(successor);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the path that branches to a false value.
     *
     * @return The path that branches to a false value.
     */
    public Optional<BasicBlock> falseBranch() {
        BasicInstruction endInsn = exit();
        if (endInsn.insn instanceof JumpInsnNode) {
            LabelNode label = ((JumpInsnNode) endInsn.insn).label;
            for (BasicBlock successor : successors) {
                BasicInstruction startInsn = successor.entry();
                if (startInsn == null || label != startInsn.insn) {
                    return Optional.of(successor);
                }
            }
        }
        return Optional.empty();
    }

    private void print(String prepend, String suffix) {
        String result = (prepend + "<" + start + " - " + end + ">\n");
        BasicInstruction[] insns = instructions();
        for (int i = 0; i < insns.length; i++) {
            if (i > 0) {
                result += "\n";
            }
            result += (prepend + Assembly.toString(insns[i].insn));
        }
        result += suffix;
        System.out.println(result);
    }

    private void printBlock(BasicBlock block, String prefix, List<BasicBlock> printed, int current, int max) {
        if (current >= max) {
            return;
        }
        if (!printed.contains(block)) {
            printed.add(block);
            boolean hasSuccessor = false;
            for (BasicBlock successor : block.successors) {
                if (!printed.contains(successor)) {
                    hasSuccessor = true;
                }
            }
            boolean brace = (hasSuccessor && current + 1 < max);
            block.print(prefix, (brace ? " { " : ""));
            if (brace) {
                block.trueBranch().ifPresent(tBranch -> {
                    if (!printed.contains(tBranch)) {
                        System.out.println(prefix + "  true: {");
                        printBlock(tBranch, prefix + "    ", printed, current + 1, max);
                        System.out.println(prefix + "  }" + (block.successors.size() > 1 ? "," : ""));
                        printed.add(tBranch);
                    }
                });
                block.falseBranch().ifPresent(fBranch -> {
                    if (!printed.contains(fBranch)) {
                        System.out.println(prefix + "  false: {");
                        printBlock(fBranch, prefix + "    ", printed, current + 1, max);
                        System.out.println(prefix + "  }");
                        printed.add(fBranch);
                    }
                });
                System.out.println(prefix + "}");
            }
        }
    }

    /**
     * Prints the block out in a readable manner.
     *
     * @param printed An empty or pre-filled list used for preventing StackOverflowExceptions
     */
    public void print(List<BasicBlock> printed, int max) {
        printBlock(this, "", printed, 0, max);
    }

    /**
     * Prints the block out in a readable manner.
     *
     * @param max The maximum amount of blocks to print out.
     */
    public void print(int max) {
        List<BasicBlock> printed = new ArrayList<>();
        print(printed, max);
    }

    /**
     * Prints the blocks out in a readable manner.
     */
    public void print() {
        print(Integer.MAX_VALUE);
    }
}