package org.firstinspires.ftc.teamcode.drive.commands;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.PrintCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.drive.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.drive.subsystems.aprilTagSubsystem.aprilTagDetector.AprilTagSubsystem;
import org.firstinspires.ftc.teamcode.drive.subsystems.driveSubsystem.DrivetrainSubsystem;
import org.firstinspires.ftc.teamcode.drive.subsystems.liftSubsystem.LiftSubsystem;
import org.firstinspires.ftc.teamcode.helpers.TrajectorySequenceSupplier;


public class AutoCommandFiveCone extends SequentialCommandGroup {
    ElapsedTime timer = new ElapsedTime();

    public AutoCommandFiveCone(DrivetrainSubsystem drivetrain, LiftSubsystem lift, IntakeSubsystem intake, AprilTagSubsystem aprilTagDetector) {

        TrajectorySequenceSupplier initToHighTrajectory = () -> drivetrain.trajectorySequenceBuilder(new Pose2d(35, -62, Math.toRadians(90)))
                .strafeTo(new Vector2d(35, -24))
                .splineToSplineHeading(new Pose2d(28, -5, Math.toRadians(135)), Math.toRadians(140)).setTangent(Math.toRadians(315))
                .build();

        TrajectorySequenceSupplier stackToHighTrajectory = () -> drivetrain.trajectorySequenceBuilder(new Pose2d(60, -12, Math.toRadians(0)))
                .strafeTo(new Vector2d(42, -12))
                .splineToSplineHeading(new Pose2d(28, -5, Math.toRadians(135)), Math.toRadians(140)).setTangent(Math.toRadians(315))
                .build();


        TrajectorySequenceSupplier parkLeft = () -> drivetrain.trajectorySequenceBuilder(new Pose2d(-28, -5, Math.toRadians(45)))
                .splineToSplineHeading(new Pose2d(-37, -30, Math.toRadians(90)), Math.toRadians(270)).setTangent(Math.toRadians(180))
                .splineToSplineHeading(new Pose2d(-60, -30, Math.toRadians(90)), Math.toRadians(180))
                .build();

        TrajectorySequenceSupplier parkCenter = () -> drivetrain.trajectorySequenceBuilder(new Pose2d(-28, -1, Math.toRadians(45)))
                .splineToSplineHeading(new Pose2d(-34, -30, Math.toRadians(90)), Math.toRadians(270))
                .build();

        TrajectorySequenceSupplier parkRight = () -> drivetrain.trajectorySequenceBuilder(new Pose2d(-28, -1, Math.toRadians(45)))
                .splineToSplineHeading(new Pose2d(-30, -30, Math.toRadians(90)), Math.toRadians(-45)).setTangent(Math.toRadians(0))
                .splineToSplineHeading(new Pose2d(-10, -30, Math.toRadians(90)), Math.toRadians(0))
                .build();

        SequentialCommandGroup initToHigh = new SequentialCommandGroup(
                new ParallelCommandGroup(
                        new FollowTrajectoryCommand(drivetrain, initToHighTrajectory).withTimeout(5000),
//                    new LiftCommand(lift, 4),
                        new IntakeCommand(intake, IntakeSubsystem.Direction.FEED)
                )
        );

        SequentialCommandGroup highToStack = new SequentialCommandGroup(
                new IntakeCommand(intake, IntakeSubsystem.Direction.UNFEED),
                new WaitCommand(1000),
                new ParallelDeadlineGroup(
                        new FollowTrajectoryCommand(drivetrain, initToHighTrajectory).withTimeout(5000),
                        new WaitCommand(1000),
//                    new TurretPositionCommand(turret, lift::getCurrentHeight, false),
//                    new LiftCommand(lift, 1),
                        new IntakeCommand(intake, IntakeSubsystem.Direction.FEED)
                )
//                new LiftCommand(lift, 0).withTimeout(500)

        );

        SequentialCommandGroup stackToHigh = new SequentialCommandGroup(
//                new LiftCommand(lift, 4).withTimeout(250),
                new ParallelCommandGroup(
                        new FollowTrajectoryCommand(drivetrain, stackToHighTrajectory).withTimeout(5000)
//                        new TurretPositionCommand(turret, lift::getCurrentHeight, true),
//                        new LiftCommand(lift, 4)
                )
        );

        Command displayTime = new InstantCommand(() -> System.out.printf("Time Left: %f.2%n", 30 - timer.time()));
        addCommands(
                new PrintCommand("Start Auto"),
                new InstantCommand(() -> {
                    timer.reset();
                    drivetrain.setPoseEstimate(new Pose2d(-62, 35, Math.toRadians(90)));
                }),
                new InstantCommand(aprilTagDetector::detect),
                // Preload
                initToHigh,
                highToStack,
                new PrintCommand("Preload Stack"),
                displayTime,
                // 1
                stackToHigh,
                highToStack,
                new PrintCommand("First Stack"),
                displayTime,
                // 2
                stackToHigh,
                highToStack,
                new PrintCommand("Second Stack"),
                displayTime,
                // 3
                stackToHigh,
                highToStack,
                new PrintCommand("Third Stack"),
                displayTime,
                // 5
                stackToHigh,

                new PrintCommand("Fourth Stack"),
                displayTime,
                new LiftCommand(lift, 0).withTimeout(1),
                // Park Left
                new ConditionalCommand(new FollowTrajectoryCommand(drivetrain, parkLeft),
                        // Park Right
                        new ConditionalCommand(new FollowTrajectoryCommand(drivetrain, parkRight),
                                // Park Center
                                new FollowTrajectoryCommand(drivetrain, parkCenter),
                                () -> aprilTagDetector.getParkLocation() == AprilTagSubsystem.Detection.RIGHT),
                        () -> aprilTagDetector.getParkLocation() == AprilTagSubsystem.Detection.LEFT
                ),
                new PrintCommand(("Parked: " + aprilTagDetector.getParkLocation().toString())),
                displayTime
        );
    }
}
