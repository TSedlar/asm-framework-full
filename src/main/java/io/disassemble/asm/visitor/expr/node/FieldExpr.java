package io.disassemble.asm.visitor.expr.node;

import org.objectweb.asm.tree.FieldInsnNode;

import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;

/**
 * @author Tyler Sedlar
 * @since 6/16/16
 *
 * A BasicExpr that represents a field.
 */
public class FieldExpr extends BasicExpr {

    private final FieldInsnNode field;

    public FieldExpr(FieldInsnNode field, int type) {
        super(field, type);
        this.field = field;
    }

    /**
     * Retrieves this field's reference key. (owner.field)
     *
     * @return This field's reference key.
     */
    public String key() {
        return (field.owner + "." + field.name);
    }

    /**
     * Checks whether this field is a GETFIELD or GETSTATIC instruction.
     *
     * @return <t>true</t> if this field is a GETFIELD or GETSTATIC instruction, otherwise <t>false</t>.
     */
    public boolean getter() {
        return (opcode() == GETFIELD || opcode() == GETSTATIC);
    }

    /**
     * Checks whether this field is a PUTFIELD or PUTSTATIC instruction.
     *
     * @return <t>true</t> if this field is a PUTFIELD or PUTSTATIC instruction, otherwise <t>false</t>.
     */
    public boolean putter() {
        return !getter();
    }
}