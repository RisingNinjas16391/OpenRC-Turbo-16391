package org.firstinspires.ftc.teamcode.drive.opmode;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.drive.subsystems.Hardware;
import org.firstinspires.ftc.teamcode.drive.subsystems.liftSubsystem.LiftConstants;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;

@RequiresApi(api = Build.VERSION_CODES.N)
@Autonomous(name="Autonomous", group="Autonomous")
//@Disabled

public class Auto extends AutonomousTemplate {
    Hardware robot = new Hardware(this::opModeIsActive, this::isStopRequested);   //Uses heavily modified untested hardware

    public void initialize() {
        Log.i("Auto", "Init hardware");
        robot.init(hardwareMap);
        robot.drivetrain.setPoseEstimate(new Pose2d(-35, -60, Math.toRadians(90)));
    }

    public void parkLeft() {
        //TODO: Trajectories for LEFT auto
        robot.drivetrain.followTrajectorySequence(
                robot.drivetrain.trajectorySequenceBuilder(robot.drivetrain.getPoseEstimate())
                        .lineToLinearHeading(new Pose2d(-58, -34,  Math.toRadians(90)))
                        .build()
        );
    }

    public void parkCenter() {
        //TODO: Trajectories for CENTER auto
        robot.drivetrain.followTrajectorySequence(
                robot.drivetrain.trajectorySequenceBuilder(robot.drivetrain.getPoseEstimate())
                        .lineToLinearHeading(new Pose2d(-34, -12, Math.toRadians(90)))
                        .lineToLinearHeading(new Pose2d(-34, -34,  Math.toRadians(90)))
                        .build()
        );
    }

    public void parkRight() {
        //TODO: Trajectories for RIGHT auto
        robot.drivetrain.followTrajectorySequence(
                robot.drivetrain.trajectorySequenceBuilder(robot.drivetrain.getPoseEstimate())
                        .lineToLinearHeading(new Pose2d(-15, -12, Math.toRadians(90)))
                        .lineToLinearHeading(new Pose2d(-15, -34,  Math.toRadians(90)))
                        .build()
        );
    }


    public void regularAutonomous() {
        //TODO: ADD AUTO
            // Set bot constraints: maxVel, maxAccel, maxAngVel, maxAngAccel, track width

        // Approaching autonomous cone stack from initialization point
        TrajectorySequence initToStack = robot.drivetrain.trajectorySequenceBuilder(robot.drivetrain.getPoseEstimate())
                .lineToLinearHeading(new Pose2d(-58, -60,  Math.toRadians(90)))
                .splineToLinearHeading(new Pose2d(-58, -12,  Math.toRadians(180)), Math.toRadians(90))
                .build();

        // Follow trajectory and set lift to intake position
        robot.drivetrain.followTrajectorySequenceAsync(initToStack);
        robot.slide.setTargetPosition(LiftConstants.feedPos);
        robot.intake.setPower(-1);
        robot.finishTrajectory(5000);

        // Lower lift to intake cone
        robot.slide.setTargetPosition(600);
        robot.finishLift(1000);

        // Clear stack
        robot.slide.setTargetPosition(LiftConstants.feedPos + 100);
        robot.finishLift(1000);

        for (int i = 0; i < 4; i ++) {
            // Approach rod from stack
            TrajectorySequence stackToHigh = robot.drivetrain.trajectorySequenceBuilder(robot.drivetrain.getPoseEstimate())
                    .lineToLinearHeading(new Pose2d(-38, -12,  Math.toRadians(180)))
                    .lineToLinearHeading(new Pose2d(-32, -7,  Math.toRadians(45)))
                    .build();

            // Follow trajectory to stack and set slides to scoring position
            robot.drivetrain.followTrajectorySequenceAsync(stackToHigh);
            robot.slide.setTargetPosition(4000);
            robot.finishTrajectory(5000);

            // Drop cone onto rod
            robot.intake.setPower(1);
            robot.wait(500);

            // Approach stack from rod
            TrajectorySequence highToStack = robot.drivetrain.trajectorySequenceBuilder(robot.drivetrain.getPoseEstimate())
                    .lineToLinearHeading(new Pose2d(-38, -12,  Math.toRadians(180)))
                    .splineToLinearHeading(new Pose2d(-58, -12,  Math.toRadians(180)), Math.toRadians(180))
                    .build();

            // Follow trajectory to stack and set lift to intake position
            robot.drivetrain.followTrajectorySequence(highToStack);
            robot.slide.setTargetPosition(LiftConstants.feedPos);
            robot.intake.setPower(-1);
            robot.finishTrajectory(5000);

            // Intake cone
            robot.slide.setTargetPosition(LiftConstants.feedPos - 500 - i * 100);
            robot.finishLift(1000);

            // Clear stack
            robot.slide.setTargetPosition(LiftConstants.feedPos + 100);
            robot.finishLift(1000);
        }
    }
}