package com.example.tripscheduler.Schedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.example.tripscheduler.R;
import com.example.tripscheduler.Server.IAppService;
import com.example.tripscheduler.Server.RetrofitClient;
import com.example.tripscheduler.Travel.Travel;

import java.util.ArrayList;

import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;

public class ScheduleListViewAdapter extends BaseAdapter {

    private ArrayList<Schedule> scheduleItemList;

    public ScheduleListViewAdapter(ArrayList<Schedule> scheduleList) {
        scheduleItemList = scheduleList;
    }

    @Override
    public int getCount() {
        return scheduleItemList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_schedule_item, parent, false);
        }

        TextView startTimeTextView = convertView.findViewById(R.id.start_time);
        TextView durationTextView = convertView.findViewById(R.id.duration);
        TextView memoTextView = convertView.findViewById(R.id.memo);

        Schedule schedule = scheduleItemList.get(position);

        String startTime;

        System.out.println("asddsa");
        System.out.println(Integer.parseInt(schedule.getData("start").replace("\"", "").split(" ")[0]));
        System.out.println("asd");

        if (Integer.parseInt(schedule.getData("start").replace("\"", "").split(" ")[0]) <= 12)
        {
            startTime = schedule.getData("start").replace("\"", "").split(" ")[0] + ":"
                    + schedule.getData("start").replace("\"", "").split(" ")[1] + "am";
        }
        else
        {
            startTime = (Integer.parseInt(schedule.getData("start").replace("\"", "").split(" ")[0]) - 12)
                    + ":" + schedule.getData("start").replace("\"", "").split(" ")[1] + "pm";
        }

        startTimeTextView.setText(startTime);
        memoTextView.setText(schedule.getData("memo").replace("\"", ""));
        durationTextView.setText(schedule.getData("duration").replace("\"", "") + "min");



        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return scheduleItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(Schedule schedule) {
        scheduleItemList.add(schedule);
    }

    public void deleteItem(Travel travel) {
        String title = travel.getTitle();
        for (int i = 0; i < scheduleItemList.size(); i++) {
            if(scheduleItemList.get(i).getTitle().equals(title)){
                scheduleItemList.remove(i);
            }
        }
    }
}
