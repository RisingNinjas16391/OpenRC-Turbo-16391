package org.firstinspires.ftc.teamcode.drive.opmode.automaticPID;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.profile.MotionProfile;
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator;
import com.acmerobotics.roadrunner.profile.MotionState;
import com.acmerobotics.roadrunner.util.NanoClock;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.teamcode.drive.DriveConstants;
import org.firstinspires.ftc.teamcode.drive.DrivetrainSubsystem;

import java.util.List;

import static org.firstinspires.ftc.teamcode.drive.DriveConstants.MAX_ACCEL;
import static org.firstinspires.ftc.teamcode.drive.DriveConstants.MAX_VEL;
import static org.firstinspires.ftc.teamcode.drive.DriveConstants.MOTOR_VELO_PID;
import static org.firstinspires.ftc.teamcode.drive.DriveConstants.RUN_USING_ENCODER;
import static org.firstinspires.ftc.teamcode.drive.DriveConstants.kV;

/*
 * This routine is designed to tune the PID coefficients used by the REV Expansion Hubs for closed-
 * loop velocity control. Although it may seem unnecessary, tuning these coefficients is just as
 * important as the positional parameters. Like the other manual tuning routines, this op mode
 * relies heavily upon the dashboard. To access the dashboard, connect your computer to the RC's
 * WiFi network. In your browser, navigate to https://192.168.49.1:8080/dash if you're using the RC
 * phone or https://192.168.43.1:8080/dash if you are using the Control Hub. Once you've successfully
 * connected, start the program, and your robot will begin moving forward and backward according to
 * a motion profile. Your job is to graph the velocity errors over time and adjust the PID
 * coefficients (note: the tuning variable will not appear until the op mode finishes initializing).
 * Once you've found a satisfactory set of gains, add them to the DriveConstants.java file under the
 * MOTOR_VELO_PID field.
 *
 * Recommended tuning process:
 *
 * 1. Increase kP until any phase lag is eliminated. Concurrently increase kD as necessary to
 *    mitigate oscillations.
 * 2. Add kI (or adjust kF) until the steady state/constant velocity plateaus are reached.
 * 3. Back off kP and kD a little until the response is less oscillatory (but without lag).
 *
 * Pressing Y/Δ (Xbox/PS4) will pause the tuning process and enter driver override, allowing the
 * user to reset the position of the bot in the event that it drifts off the path.
 * Pressing B/O (Xbox/PS4) will cede control back to the tuning process.
 */
//@Disabled
@Config
@Autonomous(group = "drive")
public class automaticPID extends LinearOpMode {
    public static double DISTANCE = 72; // in
    public double currentError = 0;
    public double previousError = 0;
    public double changeInP = 5;
    public double previousP = 0;
    public double changeInI = 1;
    public double previousI = 0;
    public double changeInD = 1;
    public double previousD = 0;
    public double previousTime = 0;
    private int cycleVal = 0;
    private double cycleTime = DriveConstants.cycleTime;
    public double actualVel;
    public double desiredVel;
    public boolean add = true;
    static ElapsedTime timer = new ElapsedTime();
    public PIDFCoefficients temp_MOTOR_VELO_PID = new PIDFCoefficients(0, 0, 0, MOTOR_VELO_PID.f);


    enum Mode {
        DRIVER_MODE,
        TUNING_MODE
    }
    enum PIDMode {
        P,
        I,
        D,
        done
    }

    private static MotionProfile generateProfile(boolean movingForward) {
        MotionState start = new MotionState(movingForward ? 0 : DISTANCE, 0, 0, 0);
        MotionState goal = new MotionState(movingForward ? DISTANCE : 0, 0, 0, 0);
        return MotionProfileGenerator.generateSimpleMotionProfile(start, goal, MAX_VEL, MAX_ACCEL);
    }

    public double errorIntegral(double actualVelocity, double desiredVelocity, double timeElapsed) {
        return Math.abs(actualVelocity-desiredVelocity) * timeElapsed;
    }

    @Override
    public void runOpMode() {
        if (!RUN_USING_ENCODER) {
            RobotLog.setGlobalErrorMsg("%s does not need to be run if the built-in motor velocity" +
                    "PID is not in use", getClass().getSimpleName());
        }

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        DrivetrainSubsystem drive = new DrivetrainSubsystem(hardwareMap);


        Mode mode = Mode.TUNING_MODE;
        PIDMode pidMode = PIDMode.P;

        drive.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, temp_MOTOR_VELO_PID);

        NanoClock clock = NanoClock.system();

        telemetry.addLine("Ready!");
        telemetry.update();
        telemetry.clearAll();

        waitForStart();

        if (isStopRequested()) return;

        boolean movingForwards = true;
        MotionProfile activeProfile = generateProfile(true);
        double profileStart = clock.seconds();


        while (!isStopRequested()) {
            telemetry.addData("mode", mode);

            switch (mode) {
                case TUNING_MODE:
                    if (gamepad1.y) {
                        mode = Mode.DRIVER_MODE;
                        drive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                    }

                    // calculate and set the motor power
                    double profileTime = clock.seconds() - profileStart;

                    if (profileTime > activeProfile.duration()) {
                        // generate a new profile
                        movingForwards = !movingForwards;
                        activeProfile = generateProfile(movingForwards);
                        profileStart = clock.seconds();
                    }

                    MotionState motionState = activeProfile.get(profileTime);
                    double targetPower = kV * motionState.getV();
                    drive.setDrivePower(new Pose2d(targetPower, 0, 0));

                    List<Double> velocities = drive.getWheelVelocities();
                    actualVel = velocities.get(0);
                    desiredVel = motionState.getV();


                    // update telemetry
                    telemetry.addData("targetVelocity", motionState.getV());
                    for (int i = 0; i < velocities.size(); i++) {
                        telemetry.addData("measuredVelocity" + i, velocities.get(i));
                        telemetry.addData(
                                "error" + i,
                                motionState.getV() - velocities.get(i)
                        );
                    }

                    switch (pidMode) {
                        case P:
                            if (timer.seconds() < cycleTime) {
                                currentError = currentError + errorIntegral(actualVel, desiredVel, timer.seconds()-previousTime);
                                previousTime = timer.seconds();
                            } else {
                                if (previousError < currentError) {
                                    add = !add;
                                    cycleVal++;
                                } else {
                                    cycleVal = 0;
                                }
                                if (add) {
                                    temp_MOTOR_VELO_PID = new PIDFCoefficients(previousP + changeInP, 0, 0, MOTOR_VELO_PID.f);
                                    previousP = previousP + changeInP;
                                } else {
                                    if (previousP-changeInP > 0) {
                                        temp_MOTOR_VELO_PID = new PIDFCoefficients(previousP - changeInP, 0, 0, MOTOR_VELO_PID.f);
                                        previousP = previousP - changeInP;
                                    } else {
                                        temp_MOTOR_VELO_PID = new PIDFCoefficients(0, 0, 0, MOTOR_VELO_PID.f);
                                        previousP = 0;

                                    }


                                }
                                timer.reset();
                                previousError = currentError;
                                drive.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, temp_MOTOR_VELO_PID);
                            }
                            if (cycleVal == 3) {
                                changeInP = changeInP/10;
                                cycleVal = 0;
                            }
                            if (changeInP == 0.05) {
                                pidMode = PIDMode.D;
                            }
                            if (cycleTime != DriveConstants.cycleTime) {
                                cycleTime = DriveConstants.cycleTime;
                            }

                            telemetry.addData("P", previousP);
                            break;
                        case I:
                            if (timer.seconds() < cycleTime) {
                                currentError = currentError + errorIntegral(actualVel, desiredVel, timer.seconds()-previousTime);
                                previousTime = timer.seconds();
                            } else {
                                if (previousError < currentError) {
                                    add = !add;
                                    cycleVal++;
                                } else {
                                    cycleVal = 0;
                                }
                                if (add) {
                                    temp_MOTOR_VELO_PID = new PIDFCoefficients(previousP, previousI + changeInI, previousD, MOTOR_VELO_PID.f);
                                    previousI = previousI + changeInI;
                                } else {
                                    if (previousI-changeInI > 0) {
                                        temp_MOTOR_VELO_PID = new PIDFCoefficients(previousP, previousI-changeInI, previousD, MOTOR_VELO_PID.f);
                                        previousI = previousI - changeInI;
                                    } else {
                                        temp_MOTOR_VELO_PID = new PIDFCoefficients(previousP, 0, previousD, MOTOR_VELO_PID.f);
                                        previousI = 0;
                                    }
                                }
                                timer.reset();
                                previousError = currentError;
                                drive.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, temp_MOTOR_VELO_PID);
                            }
                            if (cycleVal == 3) {
                                changeInI = changeInI/10;
                                cycleVal = 0;
                            }
                            if (cycleTime != DriveConstants.cycleTime) {
                                cycleTime = DriveConstants.cycleTime;
                            }
                            if (changeInI == 0.001) {
                                pidMode = PIDMode.done;
                            }

                            telemetry.addData("I", previousI);
                            break;
                        case D:
                            if (timer.seconds() < cycleTime) {
                                currentError = currentError + errorIntegral(actualVel, desiredVel, timer.seconds()-previousTime);
                                previousTime = timer.seconds();
                            } else {
                                if (previousError < currentError) {
                                    add = !add;
                                    cycleVal++;
                                } else {
                                    cycleVal = 0;
                                }
                                if (add) {
                                    temp_MOTOR_VELO_PID = new PIDFCoefficients(previousP, 0, previousD + changeInD, MOTOR_VELO_PID.f);
                                    previousD = previousD + changeInD;
                                } else {
                                    if (previousD-changeInD > 0) {
                                        temp_MOTOR_VELO_PID = new PIDFCoefficients(previousP, 0, previousD-changeInD, MOTOR_VELO_PID.f);
                                        previousD = previousD - changeInD;
                                    } else {
                                        temp_MOTOR_VELO_PID = new PIDFCoefficients(previousP, 0, 0, MOTOR_VELO_PID.f);
                                        previousD = 0;
                                    }
                                }
                                timer.reset();
                                previousError = currentError;
                                drive.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, temp_MOTOR_VELO_PID);
                            }
                            if (cycleVal == 3) {
                                changeInD = changeInD/10;
                                cycleVal = 0;
                            }
                            if (changeInD == 0.001) {
                                pidMode = PIDMode.I;
                            }
                            if (cycleTime != DriveConstants.cycleTime) {
                                cycleTime = DriveConstants.cycleTime;
                            }

                            telemetry.addData("D", previousD);
                            break;
                        case done:
                            drive.setDrivePower(new Pose2d(0, 0, 0));
                            telemetry.addData("PID", "Done! Make sure to copy these values into your code to save them.");
                    }

                    break;
                case DRIVER_MODE:
                    if (gamepad1.b) {
                        drive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

                        mode = Mode.TUNING_MODE;
                        movingForwards = true;
                        activeProfile = generateProfile(movingForwards);
                        profileStart = clock.seconds();
                    }

                    drive.setWeightedDrivePower(
                            new Pose2d(
                                    -gamepad1.left_stick_y,
                                    -gamepad1.left_stick_x,
                                    -gamepad1.right_stick_x
                            )
                    );
                    break;
            }


            telemetry.update();
        }
    }
}

