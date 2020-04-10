package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ChatRoomActivity extends AppCompatActivity
{
    /*ListView listview;
    ArrayList<String> roomsList;
    ArrayAdapter<String> arrayAdapter;*/
    EditText messageBox;
    TextView friendNameDisplay,messagesList;
    String message=null,myname,friendname,mykey,roomname;
    DatabaseReference roomref,myref;
    public void sendClicked(View view)
    {
        message=messageBox.getText().toString();
        if(message!=null)
        {
            if (roomname != null)
            {
                roomref = FirebaseDatabase.getInstance().getReference("Rooms").child(roomname);
                Map<String,Object> map=new HashMap<String, Object>();
                String temp_key = roomref.push().getKey();
                roomref.updateChildren(map);
                DatabaseReference messageroot = roomref.child(temp_key);
                Map<String, Object> map2 = new HashMap<String, Object>();
                map2.put("name", myname);
                map2.put("msg", message);
                messageroot.updateChildren(map2);
            }
            else
            {
                Toast.makeText(this, "Room unknown", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this, "Empty text", Toast.LENGTH_SHORT).show();
        }
    }
    public void appendChat(DataSnapshot dataSnapshot)
    {
        String msg,uname;
        Iterator i=dataSnapshot.getChildren().iterator();
        while (i.hasNext())
        {
            msg=(String)((DataSnapshot)i.next()).getValue();
            uname=(String)((DataSnapshot)i.next()).getValue();
            messagesList.append("\n\n"+uname+" : "+msg);
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        messagesList=(TextView)findViewById(R.id.messages);
        messageBox=(EditText)findViewById(R.id.messageBox);
        friendNameDisplay=(TextView)findViewById(R.id.friendNameDsiplay);
        myname=getIntent().getExtras().get("myname").toString();
        friendname=getIntent().getExtras().get("friendname").toString();
        friendNameDisplay.setText(friendname);
        mykey= FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        myref = FirebaseDatabase.getInstance().getReference(mykey).child(friendname);
        myref.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for (DataSnapshot snap : dataSnapshot.getChildren())
                {
                    roomname=snap.getKey().toString();
                    Toast.makeText(ChatRoomActivity.this, "roomname:"+roomname, Toast.LENGTH_SHORT).show();
                    roomref = FirebaseDatabase.getInstance().getReference("Rooms").child(roomname);
                    roomref.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                        {
                            appendChat(dataSnapshot);
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                        {
                            appendChat(dataSnapshot);
                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
                        {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                        {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }
}
