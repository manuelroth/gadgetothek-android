package ch.manuelroth.gadgetothek_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class ReservationFragment extends Fragment {

    private ListView reservationListView;
    private List<Reservation> reservationList = new ArrayList<Reservation>();
    private ReservationAdapter reservationAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_reservation, container, false);
        reservationListView = (ListView) rootView.findViewById(R.id.reservationListView);
        LibraryService.getReservationsForCustomer(new Callback<List<Reservation>>() {
            @Override
            public void notfiy(List<Reservation> input) {
                ReservationFragment.this.reservationList.addAll(input);
            }
        });

        reservationAdapter = new ReservationAdapter(this.getActivity(), R.layout.rowlayout, this.reservationList);
        reservationListView.setAdapter(reservationAdapter);
        reservationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Reservation reservation = (Reservation) parent.getItemAtPosition(position);
                AlertDialog.Builder adb=new AlertDialog.Builder(ReservationFragment.this.getActivity());
                adb.setTitle("Delete reservation");
                adb.setMessage("Are you sure you want to delete this reservation?" + position);
                final int positionToRemove = position;
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        LibraryService.deleteReservation(reservation, new Callback<Boolean>() {
                            @Override
                            public void notfiy(Boolean input) {
                                if(input){
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
                            }
                        });
                        reservationAdapter.notifyDataSetChanged();
                    }});
                adb.show();
            }
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
