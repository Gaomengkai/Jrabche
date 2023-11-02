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

package icu.merky.jrabche.fe;

public class Preprocessor {
    public static String preprocess(String source) {
        // find all starttime() and stoptime()
        // replace them with _sysy_starttime(LINENO) and _sysy_stoptime(LINENO)
        // lineno is the line number of the starttime() or stoptime() call
        // the _sysy_starttime and _sysy_stoptime functions are defined in the runtime library

        var sb = new StringBuilder();
        var lines = source.split("\n");
        for (int i = 0; i < lines.length; i++) {
            var line = lines[i];
            // starttime and stoptime may not be in a solo line
            // so we need to find them in the line
            // and replace them with _sysy_starttime and _sysy_stoptime

            // find starttime
            var index = line.indexOf("starttime()");
            if (index != -1) {
                // replace starttime with _sysy_starttime
                line = line.replace("starttime()", "_sysy_starttime(" + i + ")");
            }

            // find stoptime
            index = line.indexOf("stoptime()");
            if (index != -1) {
                // replace stoptime with _sysy_stoptime
                line = line.replace("stoptime()", "_sysy_stoptime(" + i + ")");
            }

            sb.append(line).append("\n");
        }
        return sb.toString();
    }

}
