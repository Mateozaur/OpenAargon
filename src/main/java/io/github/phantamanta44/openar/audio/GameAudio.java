package io.github.phantamanta44.openar.audio;

import io.github.phantamanta44.openar.audio.codec.CodecManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class GameAudio {

	private static final float[] EMPTY_3F = new float[] {0F, 0F, 0F};

	private long context;
	private long device;

	private int[] audioBufs;
	private int currentBuf = 0;

	private int[] srcArray;
	private FloatBuffer srcPos, srcVel;
	private FloatBuffer listPos, listVel, listOri;
	private int currentSrc = 0;

	public void init() {
		srcPos = genEmpty3F();
		srcVel = genEmpty3F();
		listPos = genEmpty3F();
		listVel = genEmpty3F();
		listOri = genFloatBuf(0F, 0F, -1F, 0F, 1F, 0F);

		device = ALC10.alcOpenDevice((ByteBuffer)null);
		if (device == 0L)
			throw new IllegalStateException("Could not open audio device!");

		ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
		if (!alcCapabilities.OpenALC10)
			throw new IllegalStateException("Audio system not supported!");

		context = ALC10.alcCreateContext(device, (int[])null);
		if (context == 0L)
			throw new IllegalStateException("Could not create audio context!");

		if (!ALC10.alcMakeContextCurrent(context))
			throw new IllegalStateException("Could not activate audio context!");

		AL.createCapabilities(alcCapabilities);

		audioBufs = new int[4];
		AL10.alGenBuffers(audioBufs);
		errorCheck("Failed to initialize audio buffers!");

		srcArray = new int[4];
		AL10.alGenSources(srcArray);
		errorCheck("Failed to initialize audio sources!");

		AL10.alListenerfv(AL10.AL_POSITION, listPos);
		AL10.alListenerfv(AL10.AL_VELOCITY, listVel);
		AL10.alListenerfv(AL10.AL_ORIENTATION, listOri);

		CodecManager.init();
	}

	private void errorCheck(String msg) {
		if (AL10.alGetError() != AL10.AL_NO_ERROR)
			throw new IllegalStateException(msg);
	}

	private int nextBuffer() {
		int bufId = audioBufs[currentBuf];
		currentBuf = (currentBuf + 1) % audioBufs.length;
		return bufId;
	}

	private int nextSrc() {
		int srcId = srcArray[currentSrc];
		currentSrc = (currentSrc + 1) % srcArray.length;
		return srcId;
	}

	void play(int format, ByteBuffer data, int freq) {
		int bId = nextBuffer(), src = nextSrc();

		AL10.alSourceStop(src);
		AL10.alSourceUnqueueBuffers(src);
		AL10.alBufferData(bId, format, data, freq);
		AL10.alSourceQueueBuffers(src, bId);

		AL10.alSourcef(src, AL10.AL_PITCH, 1F);
		AL10.alSourcef(src, AL10.AL_GAIN, 1F);
		AL10.alSourcefv(src, AL10.AL_POSITION, srcPos);
		AL10.alSourcefv(src, AL10.AL_VELOCITY, srcVel);
		AL10.alSourcePlay(src);
	}

	public void destruct() {
		if (srcArray != null)
			AL10.alDeleteSources(srcArray);

		if (audioBufs != null)
			AL10.alDeleteBuffers(audioBufs);

		ALC10.alcMakeContextCurrent(0L);

		if (context != 0L) {
			ALC10.alcDestroyContext(context);
			context = 0L;
		}

		if (device != 0L) {
			ALC10.alcCloseDevice(device);
			device = 0L;
		}
	}

	private static FloatBuffer genEmpty3F() {
		return genFloatBuf(EMPTY_3F);
	}

	private static FloatBuffer genFloatBuf(float... f) {
		return (FloatBuffer)BufferUtils.createFloatBuffer(f.length).put(f).flip();
	}

}
