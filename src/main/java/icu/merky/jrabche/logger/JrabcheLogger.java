/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2023, Gaomengkai
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package icu.merky.jrabche.logger;

public class JrabcheLogger {
    public enum LoggerLevel {D, I, W, E}

    LoggerLevel loggerLevel = LoggerLevel.D;
    public static JrabcheLogger L;

    static {
        L = new JrabcheLogger();
    }

    public JrabcheLogger() {
    }

    private void output(Object s) {
        System.out.println(s);
    }

    private void outputF(String format, Object... objects) {
        System.out.printf(format, objects);
    }

    public void setLevel(LoggerLevel loggerLevel) {
        this.loggerLevel = loggerLevel;
    }

    public void Info(Object s) {
        if (loggerLevel.ordinal() <= LoggerLevel.I.ordinal()) {
            outputF("[INFO]  ");
            output(s);
        }
    }

    public void InfoF(String s, Object... objects) {
        if (loggerLevel.ordinal() <= LoggerLevel.I.ordinal()) {
            outputF("[INFO]  ");
            outputF(s, objects);
        }
    }

    public void DebugF(String s, Object... os) {
        if (loggerLevel.ordinal() <= LoggerLevel.D.ordinal()) {
            outputF("[DEBUG] ");
            outputF(s, os);
        }
    }

    public void Debug(Object s) {
        if (loggerLevel.ordinal() <= LoggerLevel.D.ordinal()) {
            outputF("[DEBUG] ");
            output(s);

        }
    }

}
