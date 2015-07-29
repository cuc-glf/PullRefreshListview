package com.gaolf.ioslistview;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private TextView scrollIndicator;
    private MyListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollIndicator = (TextView) findViewById(R.id.scroll_indicator);
        listview = (MyListView) findViewById(R.id.listview);

        listview.setAdapter(new FakeAdapter());
        listview.setOnDragOverListener(200, new MyListView.OnDragOverListener() {
            @Override
            public void onDragOver() {
                Toast.makeText(MainActivity.this, "refresh", Toast.LENGTH_SHORT).show();
                listview.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listview.endRefresh();
                    }
                }, 3000);
            }

            @Override
            public void onDrag(float translationY) {
                scrollIndicator.setText("" + translationY);
            }
        });
    }







    private class FakeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 20;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView fakeItemView = new TextView(MainActivity.this);
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
            fakeItemView.setLayoutParams(lp);
            fakeItemView.setText("item #" + position);
            fakeItemView.setGravity(Gravity.CENTER);
            return fakeItemView;
        }
    }
}
