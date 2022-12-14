package org.firstinspires.ftc.teamcode.auto;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.drive.DriveConstants;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.utility.BaseRobot;
import org.firstinspires.ftc.teamcode.utility.Fields;
import org.firstinspires.ftc.teamcode.utility.pipelines.ZoneDetectionPipeline;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.Vector;

@Autonomous(group = "old")
public class OldTwoHighAuto extends LinearOpMode {
    // robot with drive
    BaseRobot robot = new BaseRobot();
    //opencv
    WebcamName webcamName;
    OpenCvCamera camera;
    ZoneDetectionPipeline myPipeline;
    // other
    ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() throws InterruptedException {
        robot.init(hardwareMap);

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        // OPEN CV
        webcamName = hardwareMap.get(WebcamName.class, "Webcam 1");
        camera = OpenCvCameraFactory.getInstance().createWebcam(webcamName);
        myPipeline = new ZoneDetectionPipeline(telemetry, Fields.subRectX, Fields.subRectY, Fields.subRectWidth, Fields.subRectHeight);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
                camera.setPipeline(myPipeline);
            }

            @Override
            public void onError(int errorCode) {
            }
        });

        waitForStart();

        if (isStopRequested()) return;

        robot.closeClaw();

        robot.delay(.5);
        double zone = ZoneDetectionPipeline.getZone();
        camera.stopStreaming();
        camera.closeCameraDevice();

        Pose2d startPose = new Pose2d(36, -60, Math.toRadians(90));
        drive.setPoseEstimate(startPose);
        sliderRunTo(150, Fields.sliderSpeed);
        Trajectory toHighGoal = drive.trajectoryBuilder(startPose)
                .lineTo(new Vector2d(32, -10))
                .build();
        Trajectory centerHighGoal = drive.trajectoryBuilder(toHighGoal.end())
                .lineToSplineHeading(new Pose2d(38, -2, Math.toRadians(135)),SampleMecanumDrive.getVelocityConstraint(25, DriveConstants.MAX_ANG_VEL,DriveConstants.TRACK_WIDTH), SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL))
                .build();
        Trajectory backUp = drive.trajectoryBuilder(centerHighGoal.end())
                .lineToSplineHeading(new Pose2d(38, -6, Math.toRadians(90)))
                .build();
        Trajectory toConeStack = drive.trajectoryBuilder(toHighGoal.end())
                .lineTo(new Vector2d(52, -12))
                .build();

        drive.followTrajectory(toHighGoal);
        liftHighGoal(false);
        drive.followTrajectory(centerHighGoal);
        deposit();
        closeClaw();
        //Sample Slow down
        //, SampleMEcanumDrive.getVelosityConstraint(slowerVelocity, DriveConstants.MAX_ANG_VEL,DriveConstants.TRACK_WIDTH), SAMPLEMECANUMDRIVE.getAccelerationConstaint(DriveConstants.Max_Accel)
//        drive.followTrajectory(backUp);
//        clearLift();
//        drive.followTrajectory(toConeStack);

        
    }
    // Auto robot functions
    public void liftHighGoal(boolean depositBackwards) {
        if(depositBackwards){
            sliderRunTo(Fields.sliderBackwardsHigh);
            armRunTo(Fields.armBackwardsHigh);
        } else {
            sliderRunTo(Fields.sliderForwardHigh-100);
            armRunTo(Fields.armForwardHigh);
        }
        delay(4);
    }
    public void deposit() {
        delay(2);
        robot.rightClaw.setPosition(Fields.rightClawDeliver);
        robot.leftClaw.setPosition(Fields.leftClawDeliver);
        delay(1);
    }
    // TODO this is the part that tips the entire robot over
    public void clearLift() {
        armRunTo(Fields.armBackwardsHigh, Fields.armSpeed);
        delay(1);
        sliderRunTo(Fields.sliderForwardLow);
        delay(1);
        robot.rightClaw.setPosition(Fields.rightClawPickup);
        robot.leftClaw.setPosition(Fields.leftClawPickup);

    }
    public void resetLift() {
        armRunTo(Fields.armGround, Fields.armSpeed);
        sliderRunTo(Fields.sliderGround);
        delay(1.5);
    }
    public void closeClaw() {
        robot.rightClaw.setPosition(Fields.rightClawClose);
        robot.leftClaw.setPosition(Fields.leftClawClose);
        delay(.5);
    }

    //helper functions
    private void armRunTo(int position){
        armRunTo(position, 1);
    }
    private void armRunTo(int position, double power){
        robot.arm.setTargetPosition(position);
        robot.arm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.arm.setPower(power);
    }
    private void sliderRunTo(int position){
        sliderRunTo(position, 1);
    }
    private void sliderRunTo(int position, double power){
        robot.slider.setTargetPosition(position);
        robot.slider.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.slider.setPower(power);
        robot.sideSlider.setTargetPosition(position);
        robot.sideSlider.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.sideSlider.setPower(power);
    }
    public void delay(double t) {
        runtime.reset();
        while (runtime.seconds() < t && !isStopRequested()) {
            telemetry.addLine("something");
        }
    }
}

