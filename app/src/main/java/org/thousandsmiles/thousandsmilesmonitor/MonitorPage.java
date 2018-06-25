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

import java.util.ArrayList;

public class MonitorPage {
    private ArrayList<PageColumn> m_columns = new ArrayList<PageColumn>();
    private int m_firstQueue;

    public int getFirstQueue() {
        return m_firstQueue;
    }

    public void setFirstQueue(int firstQueue) {
        m_firstQueue = firstQueue;
    }

    public void addColumn(PageColumn column) {
        m_columns.add(column);
    }

    public int columnCount() {
        return m_columns.size();
    }

    public PageColumn getColumn(int i) {
        if (i < 0 || i > columnCount() - 1) {
            return null;
        }
        return m_columns.get(i);
    }
}
