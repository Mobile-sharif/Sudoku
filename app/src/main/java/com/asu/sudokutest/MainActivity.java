package com.asu.sudokutest;

import static android.app.PendingIntent.getActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.os.Handler;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private class Cell {
        int value;
        boolean fixed;
        Button bt;

        public Cell(int initValue, Context THIS) {
            value = initValue;
            fixed = initValue != 0;
            bt = new Button(THIS);
            if (fixed) bt.setText(String.valueOf(value));
            else bt.setTextColor(getResources().getColor(R.color.blue));
            bt.setOnClickListener(v -> {
                    if (fixed) return;
                    value = selectedButton + 1;
                    if (value == 0) bt.setText("");
                    else bt.setText(String.valueOf(value));
                    if (correct()) {
                        if (completed()) {
                            tv.setText("YOU COMPLETED THE SUDOKU");
                        } else {
                            tv.setText("");
                        }
                    } else {
                        tv.setText("There is a repeated digit");
                    }
            });
        }

    }

    private class NumberButton {
        int value;
        boolean selected;
        Button bt;


        public NumberButton(int value, Context THIS) {
            this.value = value;
            bt = new Button(THIS);
            selected = false;
            bt.setText(String.valueOf(value));
            bt.setTextColor(Color.RED);
            bt.setOnClickListener(v -> {
                select();
            });
        }

        public void select() {
            if (selected) {
                selected = false;
                bt.setTextColor(ContextCompat.getColor(bt.getContext(), R.color.red));
                click(-1);
            } else {
                selected = true;
                bt.setTextColor(ContextCompat.getColor(bt.getContext(), R.color.green));
                click(value - 1);
            }
        }


        public void unselect() {
            selected = false;
            bt.setTextColor(ContextCompat.getColor(bt.getContext(), R.color.red)
            );
        }
    }


    boolean completed() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (table[i][j].value == 0) return false;
            }
        }
        return true;
    }

    boolean correct(int i1, int j1, int i2, int j2) {
        boolean[] seen = new boolean[10];
        for (int i = 1; i <= 9; i++) seen[i] = false;
        for (int i = i1; i < i2; i++) {
            for (int j = j1; j< j2; j++) {
                int value = table[i][j].value;
                if (value != 0)
                    if (seen[value]) return false;
                    else seen[value] = true;
            }
        }
        return true;
    }

    boolean correct() {
        for (int i = 0; i < 9; i++)
            if (!correct(i, 0, i + 1, 9)) return false;
        for (int j = 0; j < 9; j++)
            if (!correct(0, j, 9, j + 1)) return false;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (!correct(i * 3, j * 3, i * 3 + 3, j * 3 + 3))
                    return false;

        return true;
    }


    Cell[][] table;
    TableLayout[] tableLayouts;

    NumberButton[] numberButtons;
    int selectedButton;
    int seconds;
    String input;
    TableLayout tl;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.println(Log.ASSERT, "LOG","HERE AFTER setOnCreate");
//        input = "003020600" +
//                "900305001" +
//                "001806400" +
//                "008102900" +
//                "700000008" +
//                "006708200" +
//                "002609500" +
//                "800203009" +
//                "005010300";
        //input  = "003020600900305001001806400008102900700000008006708200002609500800203009005010300";
        input = "040895637" +
                "980000041" +
                "006140508" +
                "000674010" +
                "410500726" +
                "630209854" +
                "750061400" +
                "894027005" +
                "100450973";
        table = new Cell[9][9];
        tableLayouts = new TableLayout[9];
        tl = findViewById(R.id.tl);
        tv = findViewById(R.id.messageTv);
        seconds = 0;
        for (int i = 0; i < 3; i++) {
            TableRow tr = new TableRow(this);
            for (int j = 0; j < 3; j++) {
                int index = 3 * i + j;
                tableLayouts[index] = createTableLayout();
                tr.addView(tableLayouts[index]);
            }
            tl.addView(tr);
        }
        for (int k = 0; k < 9; k++) {
            for (int i = 0; i < 3; i++) {
                TableRow tr = new TableRow(this);
                for (int j = 0; j < 3; j++) {
                    int row = 3 * (k / 3) + i;
                    int col = 3 * (k % 3) + j;
                    table[row][col] = new Cell(Integer.parseInt(input.substring(row * 9 + col, row * 9 + col + 1)), this);
                    tr.addView(table[row][col].bt);
                }
                tableLayouts[k].addView(tr);
            }
        }
        numberButtons = new NumberButton[9];
        selectedButton =  -1;
        TableLayout numberTable = findViewById(R.id.numberTable);
        TableRow numberTr = findViewById(R.id.numberRow);
        for (int i = 0; i < 9; i++) {
            numberButtons[i] = new NumberButton(i + 1, this);
            numberTr.addView(numberButtons[i].bt);
        }
        tl.setShrinkAllColumns(true);
        tl.setStretchAllColumns(true);
        numberTable.setShrinkAllColumns(true);
        numberTable.setStretchAllColumns(true);
        runTimer();
    }

    private TableLayout createTableLayout() {
        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setBackgroundResource(R.drawable.border);
        tableLayout.setShrinkAllColumns(true);
        tableLayout.setStretchAllColumns(true);
        return tableLayout;
    }

    void click(int selectValue) {
        if (selectedButton >= 0) {
            numberButtons[selectedButton].unselect();
        }
        selectedButton = selectValue;
    }

    private void runTimer()
    {
        final TextView timeView = (TextView)findViewById(R.id.time_view);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run()
            {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                timeView.setText(time);
                seconds++;
                handler.postDelayed(this, 1000);
            }
        });
    }

}