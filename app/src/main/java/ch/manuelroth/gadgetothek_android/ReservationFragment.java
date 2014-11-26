package ch.manuelroth.gadgetothek_android;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.manuelroth.gadgetothek_android.bl.Reservation;
import ch.manuelroth.gadgetothek_android.library.Callback;
import ch.manuelroth.gadgetothek_android.library.LibraryService;

public class ReservationFragment extends Fragment {

    public ListView reservationListView;
    public List<Reservation> reservationList = new ArrayList<Reservation>();
    ReservationAdapter reservationAdapter = null;

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
