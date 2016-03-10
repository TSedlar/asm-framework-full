package io.disassemble.asm.pattern.nano.calling;

import io.disassemble.asm.pattern.nano.PatternInfo;
import io.disassemble.asm.pattern.nano.SimpleNanoPattern;
import io.disassemble.asm.ClassMethod;

/**
 * @author Tyler Sedlar
 * @since 2/1/16
 */
@PatternInfo(category = "Calling", name = "NoReturn", simple = true, description = "returns void")
public class NoReturn extends SimpleNanoPattern {

    @Override
    public boolean matches(ClassMethod method) {
        return method.desc().endsWith(")V");
    }
}