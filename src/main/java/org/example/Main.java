package org.example;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        String imageFormat = "jpg";
        int frameInterval = 1;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a video file");
        int fileResult = fileChooser.showOpenDialog(null);
        if (fileResult == JFileChooser.APPROVE_OPTION) {
            File videoFile = fileChooser.getSelectedFile();

            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setDialogTitle("Select a folder to save the images");
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int dirResult = dirChooser.showOpenDialog(null);
            if (dirResult == JFileChooser.APPROVE_OPTION) {
                File outputDir = dirChooser.getSelectedFile();
                try {
                    extractFrames(videoFile, outputDir, frameInterval, imageFormat);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Something went wrong while extracting the images from the video", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Extracts frames from a video file and saves them as images
     * @param videoFile - The video file to process
     * @param outputDir - The directory to save the extracted frames
     * @param frameInterval - The interval at which to extract frames
     * @param imageFormat - The format to save the image in
     */
    public static void extractFrames(File videoFile, File outputDir, int frameInterval, String imageFormat) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile)) {
            grabber.start();

            if (!grabber.hasVideo()) {
                JOptionPane.showMessageDialog(null, "The selected file does not contain a video", "Error", JOptionPane.ERROR_MESSAGE);
                grabber.stop();
                return;
            }

            System.out.println("Processing video with " + grabber.getLengthInFrames() + " frames ("+ grabber.getImageWidth() + "x" + grabber.getImageHeight() + ")");

            Java2DFrameConverter converter = new Java2DFrameConverter();
            Frame frame;
            int frameCounter = 0;

            while ((frame = grabber.grabImage()) != null) {
                if (frameCounter % frameInterval == 0) {
                    BufferedImage bufferedImage = converter.convert(frame);
                    if (bufferedImage != null) {
                        File outputFile = new File(outputDir, "frame-" + frameCounter + "." + imageFormat);
                        ImageIO.write(bufferedImage, imageFormat, outputFile);
                        System.out.println("Saved frame " + frameCounter + " as " + outputFile.getName());
                    }
                }
                frameCounter++;
            }

            grabber.stop();
            JOptionPane.showMessageDialog(null, "Frame extraction complete", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred during frame extraction", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}