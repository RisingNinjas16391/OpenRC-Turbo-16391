package org.firstinspires.ftc.teamcode.drive.opmode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.drive.subsystems.Hardware;
import org.firstinspires.ftc.teamcode.drive.subsystems.LinearSlideSubsystem;

@TeleOp(name = "TeleOp", group = "Teleop")
public class TeleOP extends LinearOpMode {

    Hardware robot = new Hardware();   //Uses heavily modified untested hardware

    @Override
    public void runOpMode() {

        /* Initialize the hardware variables.
         * The init() method of the hardware class does all the work here
         */

        robot.init(hardwareMap);

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Say", "Press Start to Begin");    //
        telemetry.update();
        waitForStart();
        while (opModeIsActive()) {
            editHere();
            robot.displayTelemetry(telemetry);
            // Pace this loop so jaw action is reasonable speed.
            sleep(25);
        }
    }

    public void editHere() {
        // TODO: ADD TELEOP CODE
//        double forward  = -gamepad1.left_stick_y;
//        double strafe   = -gamepad1.left_stick_x;
//        double turn     = -gamepad1.right_stick_x;
//        double[] driveValues = {
//                forward - strafe - turn,
//                forward + strafe - turn,
//                forward - strafe + turn,
//                forward + strafe + turn
//        };
//
//        robot.drivetrainSubsystem.setMotorPowers(
//                driveValues[0],
//                driveValues[1],
//                driveValues[2],
//                driveValues[3]
//        );


        double up = gamepad1.right_trigger;
        double down = -gamepad1.left_trigger;

//        robot.linearSlide.setPower(up);
//        robot.linearSlide.setPower(down);

        if (up > 0.01){
            robot.linearSlide.setPower(up);
        }
        else if (down > 0.01){
            robot.linearSlide.setPower(down);
        }
        else{
            robot.linearSlide.setPower(0);
        }
//        robot.drivetrainSubsystem.drive(forward, strafe, turn);

        }

}
