package org.firstinspires.ftc.teamcode.drive.subsystems.driveSubsystem;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequenceBuilder;

public class DrivetrainSubsystem extends SubsystemBase {

    private final MecanumDrive drivetrain;
    private final Telemetry telemetry;

    private double speedMultiplier = 1;

    public DrivetrainSubsystem(HardwareMap hwMap, Telemetry telemetry) {
        this.drivetrain = new MecanumDrive(hwMap);
        this.telemetry = telemetry;
    }

    public Pose2d getPoseEstimate() {
        return drivetrain.getPoseEstimate();
    }

    public void setPoseEstimate(Pose2d pose) {
        drivetrain.setPoseEstimate(pose);
    }

    public void drive(double forward, double strafe, double turn) {
        drivetrain.setWeightedDrivePower(new Pose2d(forward * speedMultiplier, strafe * speedMultiplier, turn * speedMultiplier));
    }

    public void driveMultiplied(double forward, double strafe, double turn, double multi) {
        drive(forward * multi, strafe * multi, turn);
    }

    public void runTrajectory(TrajectorySequence trajectory) {
        drivetrain.followTrajectorySequenceAsync(trajectory);
    }

    public TrajectorySequenceBuilder trajectorySequenceBuilder(Pose2d startPose) {
        return drivetrain.trajectorySequenceBuilder(startPose);
    }

    @Override
    public void periodic() {
        drivetrain.update();
        telemetry.addData("Drive heading", drivetrain.getPoseEstimate().getHeading());
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }
}
