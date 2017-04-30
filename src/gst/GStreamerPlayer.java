/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 *
 */
package gst;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

import java.io.File;

import org.gstreamer.Buffer;
import org.gstreamer.Bus;
import org.gstreamer.GstObject;
import org.gstreamer.State;
import org.gstreamer.elements.PlayBin2;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.Gdx;
import com.sun.jna.Native;

public class GStreamerPlayer {

    private int videoWidth, videoHeight;
    private int texture;
    public boolean done = true;
    PlayBin2 playbin;
    private final Object lock = new Object();
    private Thread currentThread;
    private Buffer buffer;

    public GStreamerPlayer(File fin)
    {
    	if (!fin.exists())
    	{
    		System.err.println("Gstreamer cannot play: "+fin+" FILE NOT FOUND");
    		return;
    	}
    	
        playbin = new PlayBin2("VideoPlayer");
        
        
        glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		//glOrtho2D(0, Gdx.graphics.getWidth(), 0, Gdx.graphics.getHeight());
//		org.lwjgl.opengl.GL11.glOrtho(left, right, bottom, top, zNear, zFar);
		GL11.glOrtho(0, Gdx.graphics.getWidth(), 0, Gdx.graphics.getHeight(), 1, -1);
		glTranslatef(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 0.0f);

		glScalef(1.0f, -1.0f, 1.0f);
		glTranslatef(-Gdx.graphics.getWidth()/2, -Gdx.graphics.getHeight()/2, 0.0f);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		

        BufferListener listener = new BufferListener();
        playbin.setInputFile(fin);

        BufferDataAppSink sink = new BufferDataAppSink("sink", listener);
        sink.setAutoDisposeBuffer(false);

        playbin.setVideoSink(sink);

        playbin.setState(State.PAUSED);
        playbin.getState();

        playbin.getBus().connect(new Bus.ERROR() {

            @Override
            public void errorMessage(GstObject arg0, int arg1, String arg2) {
                // PANIC !
            	System.err.println("GST Error: "+arg2);
                done = true;
            }
        });

        playbin.getBus().connect(new Bus.EOS() {

            @Override
            public void endOfStream(GstObject arg0) {
                done = true;
                currentThread = null;
                Native.detach(true);
            }
        });

    }
    
    public void pause()
    {
    	if (playbin.isPlaying())
    		playbin.pause();
    	else
    		playbin.play();
    }
    
    public void stop() {
        playbin.stop();
    }

    public void play()
    {
    	playbin.pause();
        playbin.getState();
        playbin.play();
        done = false;
    }
    
//    public void loop() {
//        playbin.l;
//    }

    public void destroy() {
    	if (playbin.isPlaying()) playbin.stop();
        playbin.setState(org.gstreamer.State.NULL);
        playbin.dispose();
    }

    public int getWidth() {
        return videoWidth;
    }

    public int getHeight() {
        return videoHeight;
    }

    public PlayBin2 getPlayBin() {
        return playbin;
    }

    public boolean isDone() {
        return done;
    }

    public void updateAndRender()
    {
        glClearColor(0, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT);

        if (videoWidth == 0 || videoHeight == 0)
        {
            return;
        }

        glEnable(GL_TEXTURE_2D);

        synchronized (lock)
        {
            if (texture == 0)
            {
                texture = glGenTextures();

                glBindTexture(GL_TEXTURE_2D, texture);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, videoWidth, videoHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, BufferUtils.createByteBuffer(videoWidth * videoHeight * 3));
            }
            
            if (buffer != null)
            {
            	glBindTexture(GL_TEXTURE_2D, texture);
            	GL11.glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, videoWidth, videoHeight, GL_RGB, GL_UNSIGNED_BYTE, buffer.getByteBuffer());
                buffer.dispose();
                buffer = null;
            }
        }
        glBindTexture(GL_TEXTURE_2D, texture);

        float scaleFactor = 1.0f;
        if (Display.getWidth() >= Display.getHeight()) {
            scaleFactor = (float) Display.getHeight() / (float) videoHeight;
        } else {
            scaleFactor = (float) Display.getWidth() / (float) videoWidth;
        }
        
//        float aspectRatio = (float) videoWidth / (float) videoHeight;

        float x1, x2, y1, y2;
        
        y1 = (Display.getHeight() - (videoHeight * scaleFactor)) / 2.0f;
        y2 = y1 + videoHeight * scaleFactor;

        x1 = (Display.getWidth() - (videoWidth * scaleFactor)) / 2.0f;
        x2 = x1 + videoWidth * scaleFactor;
        
        //Never needed to flip since we reset the projection matrix everytime in here manually
//        if (yFlipped)
//        {
//        	float ble = y2;
//        	y2 = y1;
//        	y1 = ble;
//        }
        
      glBegin(GL_QUADS);
      {
          glTexCoord2f(0, 0);
          glVertex2f(x1, y1);

          glTexCoord2f(1, 0);
          glVertex2f(x2, y1);

          glTexCoord2f(1, 1);
          glVertex2f(x2, y2);

          glTexCoord2f(0, 1);
          glVertex2f(x1, y2);
      }
      glEnd();

    }


    private class BufferListener implements BufferDataAppSink.Listener {

        @Override
        public void rgbFrame(int width, int height, Buffer buf)
        {
            synchronized (lock)
            {
                if (currentThread != Thread.currentThread())
                {
                    if (currentThread != null) {
                        System.out.println("Switching thread.");
                    }
                    currentThread = Thread.currentThread();
                    Native.detach(false);
                }
                if (videoWidth != width || videoHeight != height)
                {
                    System.out.println("Setting width & height : " + width + "x" + height);
                    videoWidth = width;
                    videoHeight = height;
                }
                if (buffer != null) {
                    buffer.dispose();
                }
                buffer = buf;
            }
        }
    }
}