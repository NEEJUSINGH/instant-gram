package com.example.iknownothing.instantgram;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {

    private CircleImageView CommentProfile;
    private EditText WriteComment;
    private RecyclerView CommentList;
    private ImageView CommentButton;
    private DatabaseReference UserRef,PostRef;
    private FirebaseAuth mAuth;
    String PostKey,CurrentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();

        PostKey = getIntent().getExtras().getString("PostKey");

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey).child("Comments");

        //Initialising objects;
        CommentProfile = findViewById(R.id.comment_photo);
        WriteComment = findViewById(R.id.write_comment);
        CommentButton = findViewById(R.id.postcomment);

        CommentList = findViewById(R.id.show_comments);
        CommentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        CommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //HIDING THE KEYBOARD WHEN BUTTON IS CLICKED....
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);



                UserRef.child(CurrentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            String userName = dataSnapshot.child("username").getValue().toString();
                            //String profileImage = dataSnapshot.child("profileImage").getValue().toString();
                            ValidateComment(userName);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    private void ValidateComment(String userName) {
        String commentText = WriteComment.getText().toString();
        if(TextUtils.isEmpty(commentText))
        {
            Toast.makeText(CommentActivity.this,"Enter Valid Comment",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar  calForDAte =Calendar.getInstance();

            Long tsLong = System.currentTimeMillis()/1000;
            final String ts = tsLong.toString();

            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String CurrentDate = currentDate.format(calForDAte.getTime());

            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String CurrentTime = currentTime.format(calForDAte.getTime());

            final String RandomKey = CurrentUserId+CurrentDate+CurrentTime;

            //Making data for node to store in firebase.....
            HashMap commentMap = new HashMap();
            commentMap.put("uid", CurrentUserId);
            commentMap.put("username",userName);
            commentMap.put("commenttext",commentText);
            commentMap.put("date", CurrentDate);
            commentMap.put("time", CurrentTime);
            commentMap.put("timestamp",ts);

            PostRef.child(RandomKey).updateChildren(commentMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
               if(task.isSuccessful())
               {
                   Toast.makeText(CommentActivity.this,"Comment Posted",Toast.LENGTH_SHORT).show();
               }
               else{
                   Toast.makeText(CommentActivity.this,"Unable to Comment",Toast.LENGTH_SHORT).show();
               }

                }
            });
        }





    }
}
