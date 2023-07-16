package chris.davison.cdweather.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import chris.davison.cdweather.R;

public class TodaysWeatherAdapter extends RecyclerView
        .Adapter<TodaysWeatherAdapter.TodaysWeatherViewHolder> {
    private final List<String> time;
    private final List<Bitmap> icon;
    private final List<String> temp;

    public TodaysWeatherAdapter(List<String> time, List<Bitmap> icon, List<String> temp) {
        this.time = time;
        this.icon = icon;
        this.temp = temp;
    }

    @NonNull
    @Override
    public TodaysWeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                      int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_today_weather_item, parent, false);

        return new TodaysWeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodaysWeatherViewHolder holder,
                                 int position) {
        holder.timeTv.setText(time.get(position));

        holder.iconIv.setImageBitmap(icon.get(position));

        holder.tempTv.setText(temp.get(position));
    }

    @Override
    public int getItemCount() {
        return time.isEmpty() ? 0 : time.size();
    }

    public static class TodaysWeatherViewHolder extends RecyclerView.ViewHolder {
        TextView timeTv;
        ImageView iconIv;
        TextView tempTv;

        public TodaysWeatherViewHolder(View itemView) {
            super(itemView);
            timeTv = itemView.findViewById(R.id.weatherItemTimeTv);
            iconIv = itemView.findViewById(R.id.weatherItemIconIv);
            tempTv = itemView.findViewById(R.id.weatherItemTempTv);
        }
    }
}
