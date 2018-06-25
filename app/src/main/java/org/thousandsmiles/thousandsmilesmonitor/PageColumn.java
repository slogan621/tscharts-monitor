package org.thousandsmiles.thousandsmilesmonitor;

/*
 * (C) Copyright Syd Logan 2018
 * (C) Copyright Thousand Smiles Foundation 2018
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class PageColumn {
    private int m_queue;
    private int m_offset;
    private String m_header;
    private Boolean m_isOverflow;

    public Boolean getOverflow() {
        return m_isOverflow;
    }

    public void setOverflow(Boolean overflow) {
        m_isOverflow = overflow;
    }

    public int getQueue() {
        return m_queue;
    }

    public void setQueue(int queue) {
        m_queue = queue;
    }

    public int getOffset() {
        return m_offset;
    }

    public void setOffset(int offset) {
        m_offset = offset;
    }

    public String getHeader() {
        return m_header;
    }

    public void setHeader(String header) {
        m_header = header;
    }
}
