/*
 * (C) Copyright Syd Logan 2019
 * (C) Copyright Thousand Smiles Foundation 2019
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

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ActionDialogAdapter extends BaseAdapter {

    ArrayList<Integer> m_actionIds = new ArrayList<Integer>();
    ArrayList<Integer> m_actionTextIds = new ArrayList<Integer>();
    private boolean m_isXray = false;

    public void initialize(boolean val) {
        m_isXray = val;
        if (m_isXray == true) {
            m_actionIds.add(R.drawable.xray_selector);
            m_actionTextIds.add(R.string.msg_button_remove_from_queue);
        } else {
            m_actionIds.remove(R.drawable.xray_selector);
            m_actionTextIds.remove(R.string.msg_button_remove_from_queue);
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout btnLO = new LinearLayout(parent.getContext());

        LinearLayout.LayoutParams paramsLO = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        paramsLO.setMargins(0, 0, 0, 0);

        btnLO.setOrientation(LinearLayout.VERTICAL);
        btnLO.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageView i = new ImageView(parent.getContext());

        i.setImageResource(m_actionIds.get(position));
        ((ImageView) i).setScaleType(ImageView.ScaleType.FIT_CENTER);
        final int w = (int) (36 * parent.getResources().getDisplayMetrics().density + 0.5f);
        i.setLayoutParams(new GridView.LayoutParams(w * 2, w * 2));
        btnLO.addView(i, paramsLO);

        TextView tv=new TextView(parent.getContext());
        tv.setText(parent.getContext().getString(m_actionTextIds.get(position)));
        btnLO.addView(tv, paramsLO);

        return (View) btnLO;
    }

    public final int getCount() {
        int ret = m_actionIds.size();
        return ret;
    }

    public final Object getItem(int position) {
        return null;
    }

    public final long getItemId(int position) {
        return position;
    }
}
