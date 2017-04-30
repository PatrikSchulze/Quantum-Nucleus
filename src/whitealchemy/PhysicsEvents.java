package whitealchemy;


public interface PhysicsEvents
{
	// running against a wall at full speed, may used for appropriate animation
	public void eventRunningIntoWallToLeft(QEntity entity);
	
	// running against a wall at full speed, may used for appropriate animation
	public void eventRunningIntoWallToRight(QEntity entity);

	// dropping to ground event
	public void eventLandingOnGround(QEntity entity);

	// the apex of the jump has been reached
	public void eventApex(QEntity entity);

	// starting to jump
	public void eventStartJumping(QEntity entity);

	// dropping to ground event
	public void eventDroppingDown(QEntity entity);

	// pushing against a wall constantly, LEFT
	public void eventPushingWallToLeft(QEntity entity);

	// pushing against a wall constantly, RIGHT
	public void eventPushingWallToRight(QEntity entity);
	
	
	public void eventStartRunningToLeft(QEntity entity);
	
	public void eventStartRunningToRight(QEntity entity);
	
	public void eventStoppedRunning(QEntity entity);

}
