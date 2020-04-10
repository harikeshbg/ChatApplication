package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
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

public class MainActivity extends AppCompatActivity
{
    ListView listview;
    ArrayList<String> roomsList;
    ArrayAdapter<String> arrayAdapter;
    EditText roomName;
    String name,userkey;
    FirebaseAuth auth;
    DatabaseReference root,roomref,userstable;
    int flag,userfound;
    public void logoutClicked(View view)
    {
        FirebaseAuth.getInstance().signOut();
        finish();
    }
    public void addClicked(View view)
    {
        flag=0;
        userfound=0;
        final String friendname=roomName.getText().toString();
        if (TextUtils.isEmpty(friendname))
        {
            roomName.setError("Required nickname");
            return;
        }
        userstable=FirebaseDatabase.getInstance().getReference("Users");
        userstable.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot snap : dataSnapshot.getChildren())
                    {
                        String username = snap.getKey();
                        if (username.equals(friendname))
                        {
                            Toast.makeText(MainActivity.this, "Contact saved", Toast.LENGTH_SHORT).show();
                            userfound = 1;
                            break;
                        }
                    }
                    if (userfound == 0)
                    {
                        Toast.makeText(MainActivity.this, "User doesn't exist", Toast.LENGTH_SHORT).show();
                    }
                    else if(userfound==1)
                         {
                             final String normalroomname=name+"-"+friendname;
                             final String reversedroomname=friendname+"-"+name;
                             roomref= FirebaseDatabase.getInstance().getReference("Rooms");
                             roomref.addValueEventListener(new ValueEventListener()
                             {
                                 @Override
                                 public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                 {
                                     if(dataSnapshot.exists())
                                     {
                                         for (DataSnapshot snap : dataSnapshot.getChildren())
                                         {
                                             String dbroomname = snap.getKey();
                                             if (reversedroomname.equals(dbroomname))
                                             {
                                                 Toast.makeText(MainActivity.this, "Room exists", Toast.LENGTH_SHORT).show();
                                                 DatabaseReference friend=FirebaseDatabase.getInstance().getReference(userkey).child(friendname);
                                                 friend.child(reversedroomname).setValue("");
                                                 flag = 1;
                                                 break;
                                             }
                                         }
                                         if (flag == 0)
                                         {
                                             roomref.child(normalroomname).setValue("");
                                             DatabaseReference friend=FirebaseDatabase.getInstance().getReference(userkey).child(friendname);
                                             friend.child(normalroomname).setValue("");
                                         }
                                     }
                                     else
                                     {
                                         roomref.child(normalroomname).setValue("");
                                     }
                                 }

                                 @Override
                                 public void onCancelled(@NonNull DatabaseError databaseError)
                                 {

                                 }
                             });
                             Map<String,Object> map=new HashMap<String,Object>();
                             map.put(roomName.getText().toString(),"");
                             root.updateChildren(map);
                         }

                }
                else
                {
                    Toast.makeText(MainActivity.this, "User doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userkey=FirebaseAuth.getInstance().getCurrentUser().getUid();
        root= FirebaseDatabase.getInstance().getReference(userkey);
        listview=(ListView)findViewById(R.id.roomsList);
        roomsList=new ArrayList<>();
        arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,roomsList);
        roomName=(EditText)findViewById(R.id.roomName);
        listview.setAdapter(arrayAdapter);
        requestUserName();
        root.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Set<String> set=new HashSet<>();
                Iterator i=dataSnapshot.getChildren().iterator();
                while (i.hasNext())
                {
                    set.add(((DataSnapshot)i.next()).getKey());
                }
                roomsList.clear();
                roomsList.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent=new Intent(getApplicationContext(),ChatRoomActivity.class);
                intent.putExtra("friendname",((TextView)view).getText().toString());
                intent.putExtra("myname",name);
                startActivity(intent);
            }
        });
    }
    private void requestUserName()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Enter your nickname:");
        final EditText input_field=new EditText(this);
        builder.setView(input_field);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                name=input_field.getText().toString();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                requestUserName();
            }
        });
        builder.show();
    }
}
