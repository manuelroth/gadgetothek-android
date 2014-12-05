package ch.manuelroth.gadgetothek_android;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCard;
import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCardThumbnail;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardViewNative;


public class GadgetsFragment extends Fragment {
    GadgetCardAdapter cardArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gadgets, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<Card> cards = createExampleCards();
        cardArrayAdapter = new GadgetCardAdapter(getActivity(), cards);

        ListView listView = (ListView) getActivity().findViewById(R.id.gadgetsListView);
        listView.setAdapter(cardArrayAdapter);
    }

    public List<Card> createExampleCards() {
        List<Card> list = new ArrayList<>();

        MaterialLargeImageCard cardIphone6 = MaterialLargeImageCard.with(getActivity())
                .setTextOverImage("iPhone 6")
                .useDrawableUrl("http://www.buro247.com/images/iphone-6s.jpg")
                .build();
        list.add(cardIphone6);

        MaterialLargeImageCard cardOculusRift = MaterialLargeImageCard.with(getActivity())
                .setTextOverImage("Oculus Rift Headset")
                .useDrawableUrl("http://www.independent.co.uk/incoming/article9033088.ece/alternates/w620/Oculus_Rift.jpg")
                .build();
        list.add(cardOculusRift);

        MaterialLargeImageCard cardRapberryPi = MaterialLargeImageCard.with(getActivity())
                .setTextOverImage("Raspberry Pi")
                .useDrawableUrl("http://www.raspberrypischool.org.uk/wp-content/uploads/2013/03/RaspiModelB_photo.jpeg")
                .build();
        list.add(cardRapberryPi);

        return list;
    }

    private class GadgetCardAdapter extends CardArrayAdapter {
        public GadgetCardAdapter(Context context, List<Card> cards){
            super(context, cards);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = View.inflate(getContext(), R.layout.list_material_large_image_card_layout, null);
            }

            Card card = getItem(position);
            CardViewNative cardView = (CardViewNative) convertView.findViewById(R.id.materialcard_largeimage_text);
            cardView.setCard(card);

            return convertView;
        }
    }
}
