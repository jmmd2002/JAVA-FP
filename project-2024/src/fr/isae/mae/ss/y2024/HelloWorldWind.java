/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
package fr.isae.mae.ss.y2024;

import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;

public class HelloWorldWind {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("JOGL Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GLProfile profile = GLProfile.get(GLProfile.GL2);
            GLCapabilities capabilities = new GLCapabilities(profile);

            GLCanvas canvas = new GLCanvas(capabilities);
            canvas.addGLEventListener(new GLEventListener() {
                @Override
                public void init(GLAutoDrawable drawable) {
                    System.out.println("OpenGL initialized: " + drawable.getGL().getGLProfile());
                }
                @Override
                public void dispose(GLAutoDrawable drawable) {}
                @Override
                public void display(GLAutoDrawable drawable) {}
                @Override
                public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
            });

            frame.getContentPane().add(canvas);
            frame.setSize(800, 600);
            frame.setVisible(true);
        });
    }
}
