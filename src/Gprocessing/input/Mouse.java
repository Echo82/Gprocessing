package Gprocessing.input;

import static org.lwjgl.glfw.GLFW.*;
import java.nio.DoubleBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import Gprocessing.graphics.Window;
import Gprocessing.physics.Vector2;

public class Mouse {

	public static Vector2 mouse;
	public static long mouseX = 0;
	public static long mouseY = 0;
	public static Vector2 pmouse;
	public static long pmouseX = 0;
	public static long pmouseY = 0;
	public static Vector2 mouseScroll;
	public static double scrollX = 0;
	public static double scrollY = 0;
	public static boolean mouseButton[] = new boolean[3];
	public static boolean mouseDragged;

	private static int _button;
	private static int _action;

	public static void pollMouseButtons() {
		glfwSetMouseButtonCallback(Window.window, new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				_button = button;
				_action = action;
			}
		});

		if (_action == GLFW_PRESS) {
			if (_button < mouseButton.length)
				mouseButton[_button] = true;
		} else if (_action == GLFW_RELEASE) {
			if (_button < mouseButton.length) {
				mouseButton[_button] = false;
				mouseDragged = false;
			}
		}
	}

	public static void pollMouseScroll() {
		glfwSetScrollCallback(Window.window, new GLFWScrollCallback() {
			@Override
			public void invoke(long window, double xOffset, double yOffset) {
				scrollX = xOffset;
				scrollY = yOffset;
				mouseScroll = new Vector2(scrollX, scrollY);
			}
		});
	}
	
	public static void update() {
		
		pollMouseButtons();
		pollMouseScroll();
		
		DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
		DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

		glfwGetCursorPos(Window.window, x, y);
		x.rewind();
		y.rewind();

		long pmouseX = mouseX;
		long pmouseY = mouseY;
		pmouse = new Vector2(pmouseX, pmouseY);

		mouseX = (long) x.get();
		mouseY = (long) y.get();
		mouse = new Vector2(mouseX, mouseY);
		
		if (mouseX != pmouseX || mouseY != pmouseY) {
			mouseDragged = mouseButton[0] || mouseButton[1] || mouseButton[2]; 
		}
	}
	
	public static boolean mouseButtonDown (int button) {
		if (button < mouseButton.length) {
			return mouseButton[button];
		} else {
			return false;
		}
	}
	
	public static void clearMouseInput () {
		scrollX = 0;
		scrollY = 0;
		mouseScroll = new Vector2(scrollX, scrollY);
		pmouseX = mouseX;
		pmouseY = mouseY;
		pmouse = new Vector2(pmouseX, pmouseY);
	}

	public static void printMouseLocation() {
		Gprocessing.util.Engine.println("mouseX: " + mouseX + "\tmouseY: " + mouseY);
	};

}
