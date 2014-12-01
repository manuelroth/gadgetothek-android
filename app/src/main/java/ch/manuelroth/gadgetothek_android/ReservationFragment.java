package ch.manuelroth.gadgetothek_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.manuelroth.gadgetothek_android.bl.Reservation;
import ch.manuelroth.gadgetothek_android.library.Callback;
import ch.manuelroth.gadgetothek_android.library.LibraryService;
import com.melnykov.fab.FloatingActionButton;

public class ReservationFragment extends Fragment {

    private ListView reservationListView;
    private ReservationAdapter reservationAdapter;

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
            Context context = ReservationFragment.this.getActivity().getApplicationContext();
            Intent intent = new Intent(context, ReservationActivity.class);
            startActivity(intent);
        });

        LibraryService.getReservationsForCustomer(input -> {
            ReservationFragment.this.getActivity().runOnUiThread(() -> {
                reservationAdapter.clear();
                reservationAdapter.addAll(input);
            });
        });
        reservationListView.setOnItemClickListener((parent, view, position, id) -> {
            final Reservation reservation = (Reservation) parent.getItemAtPosition(position);
            AlertDialog.Builder adb=new AlertDialog.Builder(ReservationFragment.this.getActivity());
            adb.setTitle("Delete reservation");
            adb.setMessage("Are you sure you want to delete this reservation?");
            final int positionToRemove = position;
            adb.setNegativeButton("Cancel", null);
            adb.setPositiveButton("Ok", (dialog, which) -> LibraryService.deleteReservation(reservation, input -> {
                if(input){
                    reservationAdapter.remove(reservation);
                    Context context = ReservationFragment.this.getActivity().getApplicationContext();
                    CharSequence text = "Reservation deleted";
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(context, text, duration).show();
                }else{
                    Context context = ReservationFragment.this.getActivity().getApplicationContext();
                    CharSequence text = "Reservation could not be deleted!";
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(context, text, duration).show();
                }
            }));
            adb.show();
        });

        return rootView;
    }

    private class ReservationAdapter extends ArrayAdapter<Reservation> {
        private final Context context;
        private final List<Reservation> values;

        public ReservationAdapter(Context context, int textViewResourceId, List<Reservation> values){
            super(context, textViewResourceId, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Reservation reservation = values.get(position);

            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater) ReservationFragment.this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.rowlayout, null);
            }

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
