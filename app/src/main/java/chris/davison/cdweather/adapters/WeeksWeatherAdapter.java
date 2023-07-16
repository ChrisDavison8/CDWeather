package chris.davison.cdweather.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import chris.davison.cdweather.R;

public class WeeksWeatherAdapter extends RecyclerView
        .Adapter<WeeksWeatherAdapter.WeeksWeatherViewHolder> {
    private final List<String> day;
    private final List<Bitmap> icon;
    private final List<String> tempHigh;
    private final List<String> tempLow;


    public WeeksWeatherAdapter(List<String> day, List<Bitmap> icon, List<String> tempHigh,
                               List<String> tempLow) {
        this.day = day;
        this.icon = icon;
        this.tempHigh = tempHigh;
        this.tempLow = tempLow;
    }

    @NonNull
    @Override
    public WeeksWeatherAdapter.WeeksWeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_week_weather_item, parent, false);

        return new WeeksWeatherAdapter.WeeksWeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeeksWeatherAdapter.WeeksWeatherViewHolder holder,
                                 int position) {
        holder.dayTv.setText(day.get(position));
        holder.iconIv.setImageBitmap(icon.get(position));
        holder.tempLowTv.setText(String.format(Locale.ENGLISH, "%.0f\u2103",
                (Double.parseDouble(tempLow.get(position)))));
        holder.tempHighTv.setText(String.format(Locale.ENGLISH, "%.0f\u2103",
                (Double.parseDouble(tempHigh.get(position)))));
        int i = (int) ((Double.parseDouble(tempLow.get(position))+
                Double.parseDouble(tempHigh.get(position)))/2);
        if (i <= 0) {
            holder.tempScaleSb.setProgress(0);
        } else if (i >= 40) {
            holder.tempScaleSb.setProgress(40);
        } else {
            holder.tempScaleSb.setProgress(i);
        }
    }

    @Override
    public int getItemCount() {
        return day.isEmpty() ? 0 : day.size();
    }

    public static class WeeksWeatherViewHolder extends RecyclerView.ViewHolder {
        TextView dayTv;
        ImageView iconIv;
        TextView tempHighTv;
        TextView tempLowTv;
        SeekBar tempScaleSb;

        public WeeksWeatherViewHolder(View itemView) {
            super(itemView);
            dayTv = itemView.findViewById(R.id.weekWeatherItemDayTv);
            iconIv = itemView.findViewById(R.id.weekWeatherItemIconTv);
            tempHighTv = itemView.findViewById(R.id.weekWeatherItemMaxTempTv);
            tempLowTv = itemView.findViewById(R.id.weekWeatherItemMinTempTv);
            tempScaleSb = itemView.findViewById(R.id.weekWeatherTempSb);
            tempScaleSb.setEnabled(false);
        }
    }
}
