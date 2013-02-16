package com.spriter.player;

import com.discobeard.spriter.dom.Animation;
import com.discobeard.spriter.dom.Entity;
import com.discobeard.spriter.dom.SpriterData;
import com.spriter.Spriter;
import com.spriter.SpriterKeyFrameProvider;
import com.spriter.draw.AbstractDrawer;
import com.spriter.objects.SpriterKeyFrame;

import java.util.List;

/**
 * A SpriterPlayer is the core of a spriter animation.
 * Here you can get as many information as you need.
 * 
 * SpriterPlayer plays the given data with the method {@link #update(float, float)}. You have to call this method by your own in your main game loop.
 * SpriterPlayer updates the frames by its own. See {@link #setFrameSpeed(int)} for setting the playback speed.
 * 
 * The animations can be drawn by {@link #draw()} which draws all objects with your own implemented Drawer.
 * 
 * Accessing bones and animations via names is also possible. See {@link #getAnimationIndexByName(String)} and {@link #getBoneIndexByName(String)}.
 * You can modify the whole animation or only bones at runtime with some fancy methods provided by this class.
 * Have a look at {@link #setAngle(float)}, {@link #flipX()}, {@link #flipY()}, {@link #setScale(float)} for animation moddification.
 * And see {@link #setBoneAngle(int, float)}, {@link #setBoneScaleX(int, float)}, {@link #setBoneScaleY(int, float)}.
 * 
 * All stuff you set you can also receive by the corresponding getters ;) .
 * 
 * @author Trixt0r
 */

public class SpriterPlayer extends SpriterAbstractPlayer{

	protected Entity entity;
	private Animation animation;
	private int transitionSpeed = 30;
	private int animationIndex = 0;
	private int currentKey = 0;
	SpriterKeyFrame lastRealFrame;
	boolean transitionTempFixed = true;
	private int fixCounter = 0;
	private int fixMaxSteps = 100;
	
	
	/**
	 * Constructs a new SpriterPlayer object which animates the given SpriterData.
	 * @param entity {@link Spriter} which provides a method to load all needed data to animate. See {@link Spriter#getSpriter(String, com.spriter.file.FileLoader)} for mor information.
	 * @param drawer {@link AbstractDrawer} which you have to implement on your own.
	 * @param keyframes A list of SpriterKeyFrame arrays. See {@link SpriterKeyFrameProvider#generateKeyFramePool(SpriterData)} to get the list.
	 */
	public SpriterPlayer(Entity entity, AbstractDrawer<?> drawer,List<SpriterKeyFrame[]> keyframes){
		super(drawer, keyframes);
		this.entity = entity;
		this.update(0, 0);
		this.frame = 0;
	}
	
	
	public void update(float xOffset, float yOffset){
		//Fetch information
		SpriterKeyFrame[] keyframes = this.keyframes.get(animationIndex);
		SpriterKeyFrame firstKeyFrame; 
		SpriterKeyFrame secondKeyFrame;
		if(this.transitionFixed && this.transitionTempFixed){
			if(this.frameSpeed >= 0){
				firstKeyFrame = keyframes[this.currentKey];
				secondKeyFrame = keyframes[(this.currentKey+1)%keyframes.length];
			}
			else{
				secondKeyFrame = keyframes[this.currentKey];
				firstKeyFrame = keyframes[((this.currentKey-1)+keyframes.length)%keyframes.length];
			}
			//Update
			this.frame += this.frameSpeed;
			if (this.frame > firstKeyFrame.getEndTime() && this.frameSpeed > 0){
				this.currentKey = (this.currentKey+1)%keyframes.length;
				this.frame = keyframes[this.currentKey].getStartTime();
			}
			else if(this.frame < firstKeyFrame.getStartTime()){
				this.currentKey = ((this.currentKey-1)+keyframes.length)%keyframes.length;
				this.frame = keyframes[this.currentKey].getStartTime();
			}
		}
		else{
			firstKeyFrame = keyframes[0];
			secondKeyFrame = this.lastRealFrame;
			float temp =(float)(this.fixCounter)/(float)this.fixMaxSteps;
			this.frame = this.lastRealFrame.getStartTime()+(long)(this.fixMaxSteps*temp);
			this.fixCounter= Math.min(this.fixCounter+this.transitionSpeed,this.fixMaxSteps);
			//Update
			if(this.fixCounter == this.fixMaxSteps){
				this.frame = 0;
				this.fixCounter = 0;
				if(this.lastRealFrame.equals(this.lastFrame)) this.transitionFixed = true;
				else this.transitionTempFixed = true;
				firstKeyFrame.setStartTime(0);
			}
		}
		this.currenObjectsToDraw = firstKeyFrame.getObjects().length;
		//Interpolate
		this.transformBones(firstKeyFrame, secondKeyFrame, xOffset, yOffset);		
		this.transformObjects(firstKeyFrame, secondKeyFrame, xOffset, yOffset);
	}
	
	public void setAnimatioIndex(int animationIndex, int transitionSpeed, int transitionSteps){
		if(this.animationIndex != animationIndex){
			if(this.transitionFixed){
				this.lastRealFrame = this.lastFrame;
				this.transitionFixed = false;
				this.transitionTempFixed = true;
			}
			else{
				this.lastRealFrame = this.lastTempFrame;
				this.transitionTempFixed = false;
				this.transitionFixed = true;
			}
			this.transitionSpeed = transitionSpeed;
			this.fixMaxSteps = transitionSteps;
			this.lastRealFrame.setStartTime(this.frame+1);
			this.lastRealFrame.setEndTime(this.frame+this.fixMaxSteps-1);
			this.keyframes.get(animationIndex)[0].setStartTime(this.frame+1+this.fixMaxSteps);
			this.currentKey = 0;
			this.fixCounter = 0;
			this.animationIndex = animationIndex;
			this.animation = this.entity.getAnimation().get(animationIndex);
		}
	}
	
	/**
	 * Searches for the animation index with the given name and returns the right one
	 * @param name name of the animation.
	 * @return index of the animation if the given name was found, otherwise it returns -1
	 */
	public int getAnimationIndexByName(String name){
		Animation anim = this.getAnimationByName(name);
		if(this.getAnimationByName(name) == null) return -1;
		else return anim.getId();
	}
	
	public Animation getAnimationByName(String name){
		List<Animation> anims = this.entity.getAnimation();
		for(Animation anim: anims)
			if(anim.getName().equals(name)) return anim;
		return null;
	}
	
	/**
	 * @return current animation index
	 */
	public int getAnimationIndex(){
		return this.animationIndex;
	}

	/**
	 * @return the spriterData
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * @param spriterData the spriterData to set
	 */
	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	
	/**
	 * @return the anim
	 */
	public Animation getAnimation() {
		return animation;
	}	
}
