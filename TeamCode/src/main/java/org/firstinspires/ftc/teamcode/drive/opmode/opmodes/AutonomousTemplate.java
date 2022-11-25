package org.firstinspires.ftc.teamcode.drive.opmode.opmodes;

import static org.firstinspires.ftc.teamcode.drive.opmode.opmodes.AutoConstants.threshold;
import static org.firstinspires.ftc.teamcode.drive.opmode.opmodes.AutoConstants.timeout;
import static org.firstinspires.ftc.teamcode.drive.subsystems.aprilTagSubsystem.aprilTagDetector.AprilTagDetectorConstants.TAG_ID_LEFT;
import static org.firstinspires.ftc.teamcode.drive.subsystems.aprilTagSubsystem.aprilTagDetector.AprilTagDetectorConstants.TAG_ID_CENTER;
import static org.firstinspires.ftc.teamcode.drive.subsystems.aprilTagSubsystem.aprilTagDetector.AprilTagDetectorConstants.TAG_ID_RIGHT;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.drive.subsystems.aprilTagSubsystem.aprilTagDetector.AprilTagDetector;
import org.openftc.apriltag.AprilTagDetection;

import java.util.ArrayList;


public abstract class AutonomousTemplate extends LinearOpMode {
    AprilTagDetector aprilTagdetector;

    @Override
    public void runOpMode() {
        initialize();
        aprilTagdetector = new AprilTagDetector(hardwareMap);
        telemetry.addData(">", "Ready when you are!");
        telemetry.update();
        Log.i("Robot", "standby");
        waitForStart();
        if (opModeIsActive()) {
            ArrayList<AprilTagDetection> detections =  aprilTagdetector.detect(timeout, threshold);
//            Map.Entry<Integer, Integer> maxDetection = null;
//            if (detections.size() > 0) {
//                Map<Integer, Integer> detectionsCount = new HashMap<>();
//                for (AprilTagDetection d: detections) {
//                    if(detectionsCount.containsKey(d.id)) {
//                        detectionsCount.put(d.id, detectionsCount.get(d.id) + 1);
//                    } else {
//                        detectionsCount.put(d.id, 0);
//                    }
//
//
//                    for (Map.Entry<Integer, Integer> entry: detectionsCount.entrySet())
//                    {
//                        if (maxDetection == null || entry.getValue().compareTo(maxDetection.getValue()) > 0)
//                        {
//                            maxDetection = entry;
//                        }
//                    }
//                }
//
//
//            }

            regularAutonomous();

            switch (detections.get(0).id) {
                case TAG_ID_LEFT:
                    parkLeft();
                    Log.i("Robot", "auto a");
                    telemetry.addLine("auto a");
                    telemetry.update();
                    break;

                case TAG_ID_CENTER:
                    parkCenter();
                    Log.i("Robot", "auto b");
                    telemetry.addLine("auto b");
                    telemetry.update();
                break;

                case TAG_ID_RIGHT:
                    parkRight();
                    Log.i("Robot", "auto c");
                    telemetry.addLine("auto c");
                    telemetry.update();
                break;

                default:
                    telemetry.addLine("No park auto");
                    telemetry.update();
                break;
            }


        }
    }

    public abstract void initialize();

    public abstract void regularAutonomous();

    public abstract void parkLeft();

    public abstract void parkCenter();

    public abstract void parkRight();
}