
/*
 * (C) Copyright Syd Logan 2017
 * (C) Copyright Thousand Smiles Foundation 2017
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

package org.thousandsmiles.thousandsmilesmonitor;

import org.json.JSONObject;

import java.util.ArrayList;

public class QueueHeader {
    public enum State {
        AWAY, ACTIVE, WAITING
    }

    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private State m_state;
    private String m_serviceTime = "";
    private String m_willReturn = "";
    private int m_activePatient;
    private boolean m_stub = false;   // if stub, then display empty header

    public void setState(State state) {
        m_state = state;
    }

    public void setStub(boolean stub) {
        m_stub = stub;
    }

    public void setActivePatient(int id) { m_activePatient = id; }

    public void setServiceTime(String timestr) {
        m_serviceTime = timestr;
    }

    public void setWillReturnTime(String timestr) {
        m_willReturn = timestr;
    }

    private String stateToLabel() {
        String label = "Error";
        switch (m_state) {
            case AWAY:
                if (m_sess.getLanguage().equals("en_US")) {
                    label = String.format(m_sess.getContext().getResources().getString(R.string.away));
                } else {
                    label = String.format(m_sess.getContext().getResources().getString(R.string.away_es));
                }
                break;
            case ACTIVE:
                if (m_sess.getLanguage().equals("en_US")) {
                    label = String.format(m_sess.getContext().getResources().getString(R.string.active));
                } else {
                    label = String.format(m_sess.getContext().getResources().getString(R.string.active_es));
                }
                break;
            case WAITING:
                if (m_sess.getLanguage().equals("en_US")) {
                    label = String.format(m_sess.getContext().getResources().getString(R.string.waiting));
                } else {
                    label = String.format(m_sess.getContext().getResources().getString(R.string.waiting_es));
                }
                break;
        }
        return label;
    }

    public ArrayList<String> getLabels()  // XXX add language argument
    {
        ArrayList<String> ret = new ArrayList<String>();
        String avgServiceTime;
        String state;
        String willReturn;

        if (m_stub == true) {
            return ret;
        }

        if (m_sess.getLanguage().equals("en_US")) {
            state = String.format(m_sess.getContext().getResources().getString(R.string.state));
        } else {
            state = String.format(m_sess.getContext().getResources().getString(R.string.state_es));
        }

        if (m_sess.getLanguage().equals("en_US")) {
            willReturn = String.format(m_sess.getContext().getResources().getString(R.string.willreturn));
        } else {
            willReturn = String.format(m_sess.getContext().getResources().getString(R.string.willreturn_es));
        }

        if (m_sess.getLanguage().equals("en_US")) {
            avgServiceTime = String.format(m_sess.getContext().getResources().getString(R.string.avgservicetime));
        } else {
            avgServiceTime = String.format(m_sess.getContext().getResources().getString(R.string.avgservicetime_es));
        }

        ret.add(String.format("%s: %s", avgServiceTime, m_serviceTime));

        ret.add(String.format("%s: %s", state, stateToLabel()));
        if (m_state == State.AWAY) {
            ret.add(String.format("%s: %s", willReturn, m_willReturn));
        }
        return ret;
    }

    public State getState() {
        return m_state;
    }
}
