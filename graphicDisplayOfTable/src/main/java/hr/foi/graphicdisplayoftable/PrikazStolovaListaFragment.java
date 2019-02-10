package hr.foi.graphicdisplayoftable;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class PrikazStolovaListaFragment extends Fragment implements PrikazStolova {
    private String name="Putem liste";
    private OnOdabirStolaCompleteListener onOdabirStolaCompleteListener;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<StoloviRecyclerAdapterItem> mListaStolova;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_lista_stolovi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        kreirajRecyclerView();

        mRecyclerView = view.findViewById(R.id.recyclerView);


        final String restoran=this.getActivity().getIntent().getStringExtra("restoranId");
        final String dolazak=this.getActivity().getIntent().getStringExtra("dolazak");
        final String odlazak=this.getActivity().getIntent().getStringExtra("odlazak");

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("restoran").child(restoran).child("stolovi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data:dataSnapshot.getChildren()){
                    final String stol=data.child("oznaka").getValue().toString();
                    final String sifraStola=restoran+"_"+data.getKey();
                    final ArrayList<String> listaStolova=new ArrayList<>();

                    databaseReference.child("rezervacija").child(sifraStola).orderByChild("odlazak").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot data:dataSnapshot.getChildren()){
                                if(Long.parseLong(data.child("odlazak").getValue().toString())>Long.parseLong(dolazak)){
                                    if(Long.parseLong(data.child("dolazak").getValue().toString())>Long.parseLong(odlazak)){
                                        if(!listaStolova.contains(stol)){
                                            listaStolova.add(stol);
                                            dodajGumb(stol);
                                        }
                                    }
                                }
                                else{
                                    if(!listaStolova.contains(stol)){
                                        listaStolova.add(stol);
                                        dodajGumb(stol);
                                    }
                                }
                            }
                            if(!dataSnapshot.exists()){
                                if(!listaStolova.contains(stol)){
                                    listaStolova.add(stol);
                                    dodajGumb(stol);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void dodajGumb(final String stol){
        int position=mListaStolova.size();
        Button button=new Button(getActivity());
        button.setText(stol);
        button.setTag(stol);

        mListaStolova.add(position, new StoloviRecyclerAdapterItem(button, this.onOdabirStolaCompleteListener));
        mAdapter.notifyItemInserted(position);
    }

    private void kreirajRecyclerView(){
        mListaStolova=new ArrayList<>();
        mRecyclerView = getActivity().findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        mAdapter = new StoloviRecyclerAdapter(mListaStolova);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setOnCompleteListener(OnOdabirStolaCompleteListener onCompleteListener) {
        this.onOdabirStolaCompleteListener=onCompleteListener;
    }
}