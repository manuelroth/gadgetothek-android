package ch.manuelroth.gadgetothek_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.manuelroth.gadgetothek_android.bl.Reservation;
import ch.manuelroth.gadgetothek_android.library.Callback;
import ch.manuelroth.gadgetothek_android.library.LibraryService;

public class ReservationFragment extends Fragment {

    private ListView reservationListView;
    private ReservationAdapter reservationAdapter;
    private ImageLoader imageLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_reservation, container, false);
        reservationListView = (ListView) rootView.findViewById(R.id.reservationListView);
        reservationAdapter = new ReservationAdapter(this.getActivity(), R.layout.rowlayout, new ArrayList<>());
        reservationListView.setAdapter(reservationAdapter);
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.attachToListView(reservationListView);
        fab.setOnClickListener(v -> {
            if(reservationAdapter.getCount() == 3){
                Context context = ReservationFragment.this.getActivity().getApplicationContext();
                CharSequence text = "Es sind immer nur drei Reservationen zur gleichen Zeit erlaubt";
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, text, duration).show();
            }else{
                Context context = ReservationFragment.this.getActivity().getApplicationContext();
                Intent intent = new Intent(context, ReservationActivity.class);
                intent.putExtra("numberOfReservations", reservationAdapter.getCount());
                startActivity(intent);
            }
        });

        LibraryService.getReservationsForCustomer(input -> {
            ReservationFragment.this.getActivity().runOnUiThread(() -> {
                reservationAdapter.clear();
                reservationAdapter.addAll(input);
            });
        });
        reservationListView.setOnItemClickListener((parent, view, position, id) -> {
            final Reservation reservation = (Reservation) parent.getItemAtPosition(position);
            AlertDialog.Builder adb = new AlertDialog.Builder(ReservationFragment.this.getActivity());
            adb.setTitle(R.string.deleteReservation);
            adb.setMessage(R.string.deleteReservationQuestion);
            final int positionToRemove = position;
            adb.setNegativeButton(R.string.cancel, null);
            adb.setPositiveButton(R.string.ok, (dialog, which) -> LibraryService.deleteReservation(reservation, input -> {
                if (input) {
                    reservationAdapter.remove(reservation);
                    Context context = ReservationFragment.this.getActivity().getApplicationContext();
                    CharSequence text = "Reservation gelöscht";
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(context, text, duration).show();
                } else {
                    Context context = ReservationFragment.this.getActivity().getApplicationContext();
                    CharSequence text = "Reservation konnte nicht gelöscht werden!";
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(context, text, duration).show();
                }
            }));
            adb.show();
        });

        File cacheDir = StorageUtils.getCacheDirectory(getActivity());
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity())
                .diskCache(new UnlimitedDiscCache(cacheDir))
                .build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        
        return rootView;
    }

    public void onTabContentChanged(){
        LibraryService.getReservationsForCustomer(new Callback<List<Reservation>>() {
            @Override
            public void notfiy(List<Reservation> input) {
                reservationAdapter.clear();
                reservationAdapter.addAll(input);
            }
        });
    }

    private class ReservationAdapter extends ArrayAdapter<Reservation> {
        private final Context context;
        private final List<Reservation> values;

        public ReservationAdapter(Context context, int textViewResourceId, List<Reservation> values) {
            super(context, textViewResourceId, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Reservation reservation = values.get(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) ReservationFragment.this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.rowlayout, null);
            }

            ImageView thumbnailView = (ImageView) convertView.findViewById(R.id.thumbnail);
            imageLoader.displayImage(reservation.getGadget().getThumbnailUrl(), thumbnailView);

            TextView gadgetNameView = (TextView) convertView.findViewById(R.id.gadgetName);
            TextView lentTillView = (TextView) convertView.findViewById(R.id.date);

            gadgetNameView.setText(reservation.getGadget().getName());
            DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            Date reservationDate = reservation.getReservationDate();
            String formattedDate = formatter.format(reservationDate);
            lentTillView.setText(formattedDate);

            return convertView;
        }
    }
}
