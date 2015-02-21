package com.bingo.fibonacci.bingo;

import android.support.v4.app.Fragment;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;


public class BingoFragment extends Fragment implements AdapterView.OnItemClickListener {

    private boolean[] table = new boolean[25];
    private boolean first_bingo = true;

    public interface Listener {
            public void onBingo();
    }

    Listener mListener = null;
    boolean mShowSignIn = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bingo_board, container, false);

        GridView gridview = (GridView) v.findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(getActivity().getApplicationContext()));

        gridview.setOnItemClickListener(this);
        first_bingo = true;
        table = new boolean[25];
        return v;
    }

    public void setListener (Listener l) { mListener = l; }


    private boolean isBingo() {
        boolean win = false;
        for (int i = 0; i <= 20; i = i + 5) {
            win |= table[i] && table[1 + i] && table[2 + i] && table[3 + i] && table[4 + i];
            if (win)
                return win;

        }
        for (int i = 0; i < 5; i++) {
            win |= table[i] && table[5 + i] && table[10 + i] && table[15 + i] && table[20 + i];
            if (win)
                return win;

        }
        win |= table[0] && table[6] && table[12] && table[18] && table[24];
        win |= table[4] && table[8] && table[12] && table[16] && table[20];
        return win;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUi();
    }

    void updateUi() {
        //if (getActivity() == null) return;
        //TextView scoreInput = ((TextView) getActivity().findViewById(R.id.score_input));
        //if (scoreInput != null) scoreInput.setText(String.format("%04d", mRequestedScore));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (table[position]) {
            ((ImageView) view).clearColorFilter();
            table[position] = false;
        } else {
            ((ImageView) view).setColorFilter(0xffa40000, PorterDuff.Mode.OVERLAY);
            table[position] = true;
            if (isBingo() && first_bingo) {
                //Toast.makeText(BingoFragment.this, "BINGO!", Toast.LENGTH_SHORT).show();
                //TODO What happens when there is a bingo
                mListener.onBingo();
                first_bingo = false;
            }
        }
    }
}