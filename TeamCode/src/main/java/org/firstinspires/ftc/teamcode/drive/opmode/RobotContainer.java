package org.firstinspires.ftc.teamcode.drive.opmode;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.gamepad1;
import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.gamepad2;
import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.telemetry;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.button.GamepadButton;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.drive.commands.AutoCommand1;
import org.firstinspires.ftc.teamcode.drive.commands.DrivetrainCommand;
import org.firstinspires.ftc.teamcode.drive.commands.IntakeCommand;
import org.firstinspires.ftc.teamcode.drive.commands.TurretCommand;
import org.firstinspires.ftc.teamcode.drive.subsystems.AprilTagSubsystem;
import org.firstinspires.ftc.teamcode.drive.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.drive.subsystems.driveSubsystem.DrivetrainSubsystem;
import org.firstinspires.ftc.teamcode.drive.subsystems.liftSubsystem.LiftSubsystem;
import org.firstinspires.ftc.teamcode.drive.subsystems.turretSubsystem.TurretSubsystem;

public class RobotContainer {
    private final DrivetrainSubsystem drivetrain;
    private final LiftSubsystem lift;
    private final IntakeSubsystem intake;
    private final TurretSubsystem turret;
    private final AprilTagSubsystem aprilTagDetector;

    private final GamepadEx driverController;
    private final GamepadEx operatorController;

    private final GamepadButton up;
    private final GamepadButton down;
    private final GamepadButton slowMode;
    private final GamepadButton turboMode;
    private final GamepadButton lockRotation;

    private final GamepadButton dropCone;
    private final GamepadButton turretToggle;

    /**
     * Teleop Constructor
     */
    public RobotContainer(HardwareMap hwMap, Gamepad gamepad1, Gamepad gamepad2, Telemetry telemetry) {
        drivetrain = new DrivetrainSubsystem(hwMap, telemetry);
        lift = new LiftSubsystem(hwMap, telemetry);
        intake = new IntakeSubsystem(hwMap, telemetry);
        turret = new TurretSubsystem(hwMap, telemetry);
        aprilTagDetector = new AprilTagSubsystem(hwMap, telemetry);

        driverController = new GamepadEx(gamepad1);

        operatorController = new GamepadEx(gamepad2);

        up = new GamepadButton(driverController, GamepadKeys.Button.DPAD_UP);

        down = new GamepadButton(driverController, GamepadKeys.Button.DPAD_DOWN);

        slowMode = new GamepadButton(driverController, GamepadKeys.Button.RIGHT_BUMPER);

        turboMode = new GamepadButton(driverController, GamepadKeys.Button.LEFT_BUMPER);

        lockRotation = new GamepadButton(driverController, GamepadKeys.Button.RIGHT_STICK_BUTTON);

        dropCone = new GamepadButton(operatorController, GamepadKeys.Button.RIGHT_BUMPER);

        turretToggle = new GamepadButton(operatorController, GamepadKeys.Button.LEFT_BUMPER);

        setDefaultCommands();
        configureButtonBindings();

    }

    /**
     * Autonomous Constructor
     */
    public RobotContainer(HardwareMap hwMap, int autoNum) {
        drivetrain = new DrivetrainSubsystem(hwMap, telemetry);
        lift = new LiftSubsystem(hwMap, telemetry);
        intake = new IntakeSubsystem(hwMap, telemetry);
        turret = new TurretSubsystem(hwMap, telemetry);
        aprilTagDetector = new AprilTagSubsystem(hwMap, telemetry);

        driverController = new GamepadEx(gamepad1);

        operatorController = new GamepadEx(gamepad2);

        up = new GamepadButton(driverController, GamepadKeys.Button.DPAD_UP);

        down = new GamepadButton(driverController, GamepadKeys.Button.DPAD_DOWN);

        slowMode = new GamepadButton(driverController, GamepadKeys.Button.RIGHT_BUMPER);

        turboMode = new GamepadButton(driverController, GamepadKeys.Button.LEFT_BUMPER);

        lockRotation = new GamepadButton(driverController, GamepadKeys.Button.RIGHT_STICK_BUTTON);

        dropCone = new GamepadButton(operatorController, GamepadKeys.Button.RIGHT_BUMPER);

        turretToggle = new GamepadButton(operatorController, GamepadKeys.Button.LEFT_BUMPER);


        setAutoCommands(autoNum);
    }

    private void configureButtonBindings() {
        // Driver bindings
        // Speed mode button binding
        slowMode.whenPressed(() -> drivetrain.setSpeedMultiplier(0.6));
        turboMode.whenActive(() -> drivetrain.setSpeedMultiplier(1));
        slowMode.negate().and(turboMode.negate()).whenActive(() -> drivetrain.setSpeedMultiplier(0.7));

        // Operator bindings
        dropCone.whileActiveOnce(new IntakeCommand(intake, false));
        //dropCone.whenPressed(new InstantCommand(intake::unfeed, intake));
        turretToggle.whenPressed(new TurretCommand(turret, lift::getCurrentHeight));
        up.whenPressed(lift::incrementHeight);
        down.whenPressed(lift::decrementHeight);
    }

    private void setDefaultCommands() {
        drivetrain.setDefaultCommand(new DrivetrainCommand(drivetrain, lift::getCurrentHeight,
                driverController::getLeftY, driverController::getLeftX,
                driverController::getRightX));

        intake.setDefaultCommand(new IntakeCommand(intake, true));
    }

    private void setAutoCommands(int chooser) {
        Command Auto1 = new AutoCommand1(drivetrain, lift, intake, aprilTagDetector);
        switch (chooser) {
            case 0:
                Auto1.schedule();
                break;
            case 1:
                break;
        }

    }
}
