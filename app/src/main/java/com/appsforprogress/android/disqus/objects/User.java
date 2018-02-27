package com.appsforprogress.android.disqus.objects;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Oswald on 1/3/2016.
 */
public class User
{
    // Define the fields for Skill Model
    private UUID mUserId;
    private String mFBUserId;
    private String mFirstName;
    private String mLastName;
    private double mLat;
    private double mLon;
    private Date mBirthDate;
    private String mName;
    private String mEmail;
    private String mFaceBookID;
    private Date mSignUpDate;
    // Determines what to show on UserProfile:
    private Boolean mQuizCompleted;
    private Uri picture;
    private String fullName;
    private String id;
    private String email;
    private String permissions;
    private ArrayList<FBLike> mFBLikes;

    // Define constructor for the Skill Model
    public User()
    {
        // Call the secondary constructor below with a random UUID:
        this(UUID.randomUUID());
    }

    public User(UUID id)
    {
        mUserId = id;
        mSignUpDate = new Date();
    }

    public User(Uri picture, String name,
                String id, String email, String permissions, ArrayList<FBLike> userLikes) {
        this.picture = picture;
        this.fullName = name;
        this.id = id;
        this.email = email;
        this.permissions = permissions;
        this.mFBLikes = userLikes;
    }

    // Getter for mId
    public UUID getUserId()
    {
        return mUserId;
    }

    // Getter and setters:
    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public double getLat()
    {
        return mLat;
    }

    public void setLat(double mLat)
    {
        this.mLat = mLat;
    }

    public double getLon()
    {
        return mLon;
    }

    public void setLon(double mLon)
    {
        this.mLon = mLon;
    }

    public Boolean getQuizCompleted()
    {
        return mQuizCompleted;
    }

    public void setQuizCompleted(Boolean quizCompleted) {
        mQuizCompleted = quizCompleted;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getFaceBookID() {
        return mFaceBookID;
    }

    public void setFaceBookID(String faceBookID) {
        mFaceBookID = faceBookID;
    }

    public Date getSignUpDate()
    {
        return mSignUpDate;
    }

    public void setSignUpDate(Date signUpDate)
    {
        mSignUpDate = signUpDate;
    }

    public Date getBirthDate() {
        return mBirthDate;
    }

    public void setBirthDate(Date birthDate) {
        mBirthDate = birthDate;
    }

    // Facebook Id usage:
    public String getFBUserId() {
        return mFBUserId;
    }

    public void setFBUserId(String FBUserId) {
        mFBUserId = FBUserId;
    }

    public Uri getPicture() {
        return picture;
    }

    public String getFullName() {
        return fullName;
    }

    public String getId() {
        return id;
    }


    public String getPermissions() {
        return permissions;
    }

    public ArrayList<FBLike> getFBLikes() {
        return mFBLikes;
    }

    public void setFBLikes(ArrayList<FBLike> mFBLikes) {
        this.mFBLikes = mFBLikes;
    }
}
