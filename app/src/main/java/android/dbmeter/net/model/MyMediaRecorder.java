package android.dbmeter.net.model;

import android.media.MediaRecorder;

import java.io.IOException;

public class MyMediaRecorder {
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false ;

    public int getMaxAmplitude() {
        if (mMediaRecorder != null) {
            try {
                return mMediaRecorder.getMaxAmplitude();
            }
            catch (IllegalArgumentException e) {
                e.printStackTrace();
                return -1;
            }
        }
        else {
            return -1;
        }
    }

    /**
     * Recording
     * @return Whether to start recording successfully
     */
    public boolean startRecorder(){
        try {
            mMediaRecorder = new MediaRecorder();

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setOutputFile("/dev/null");

            mMediaRecorder.prepare();
            mMediaRecorder.start();
            isRecording = true;

            return true;
        }
        catch(IOException exception) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            isRecording = false ;
            exception.printStackTrace();
        }
        catch(IllegalStateException e){
            stopRecording();
            e.printStackTrace();
            isRecording = false;
        }

        return false;
    }

    public void stopRecording(){
        if (mMediaRecorder != null){
            if(isRecording){
                try{
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
            mMediaRecorder = null;
            isRecording = false ;
        }
    }

    public void pauseRecording(){
        if (mMediaRecorder != null){
            if(isRecording){
                try{
                    mMediaRecorder.stop();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            isRecording = false ;
        }
    }

    public void resumeRecording(){
        if (mMediaRecorder != null){
            if(!isRecording){
                try {
                    mMediaRecorder = new MediaRecorder();

                    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    mMediaRecorder.setOutputFile("/dev/null");

                    mMediaRecorder.prepare();
                    mMediaRecorder.start();
                    isRecording = true;
                } catch(IOException exception) {
                    mMediaRecorder.reset();
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                    isRecording = false ;
                    exception.printStackTrace();
                } catch(IllegalStateException e){
                    stopRecording();
                    e.printStackTrace();
                    isRecording = false ;
                }
            }
        }
    }

    synchronized public void restartRecording(){
        if (mMediaRecorder != null){
            if(isRecording){
                try{
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    mMediaRecorder.release();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            mMediaRecorder = null;

            try {
                mMediaRecorder = new MediaRecorder();

                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mMediaRecorder.setOutputFile("/dev/null");

                mMediaRecorder.prepare();
                mMediaRecorder.start();
                isRecording = true;
            }
            catch(IOException exception) {
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
                isRecording = false ;
                exception.printStackTrace();
            }
            catch(IllegalStateException e){
                stopRecording();
                e.printStackTrace();
                isRecording = false ;
            }
        }
    }
}