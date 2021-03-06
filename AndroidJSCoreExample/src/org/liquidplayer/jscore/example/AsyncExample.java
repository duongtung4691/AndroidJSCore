//
// AsyncExample.java
// AndroidJSCore project
//
// https://github.com/ericwlange/AndroidJSCore/
//
// Created by Eric Lange
//
/*
 Copyright (c) 2014 Eric Lange. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.liquidplayer.jscore.example;

import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSException;
import org.liquidplayer.webkit.javascriptcore.JSObject;
import org.liquidplayer.webkit.javascriptcore.JSValue;

import android.os.AsyncTask;

public class AsyncExample implements IExample {
	public AsyncExample(ExampleContext ctx) {
		context = ctx;
	}
	private final ExampleContext context;

	// We can also handle asynchronous callbacks to JavaScript.  We will define a function
	// that waits a specified period of time and then makes a callback with a message
	// to be displayed by the JS code.
	//
	// This is really useful for handling long actions on the Java side.  For instance,
	// if the script need to make HTTP calls.  Since JavaScriptCore is just the isolated
	// JavaScript environment, it does not include any DOM or xmlHttp objects you may be
	// used to in the browser.  To access HTTP, you will need to expose some Java functions
	// to handle that for you.
	public interface IAsyncObj {
		public void callMeMaybe(Integer ms, JSValue callback) throws JSException;
	}
	public class AsyncObj extends JSObject implements IAsyncObj {
		public AsyncObj(JSContext ctx) throws JSException { super(ctx,IAsyncObj.class); }
		@Override
		public void callMeMaybe(Integer ms, JSValue callback) throws JSException {
			new CallMeLater(ms).execute(callback.toObject());
		}
		
		private class CallMeLater extends AsyncTask<JSObject, Void, JSObject> {
			public CallMeLater(Integer ms) {
				this.ms = ms;
			}
			private final Integer ms;
			@Override
			protected JSObject doInBackground(JSObject... params) {
				try {
					Thread.sleep(ms);
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
				return params[0];
			}

			@Override
			protected void onPostExecute(JSObject callback) {
				JSValue args [] = { new JSValue(context,"This is a delayed message from Java!") };
				try {
					callback.callAsFunction(null, args);
				} catch (JSException e) {
					System.out.println(e);
				}
			}
		}
	}
	
	public void run() throws JSException {
		context.log("Asynchrous Callback");
		context.log("---------------------");
		AsyncObj async = new AsyncObj(context);
		context.property("async",async);
		context.evaluateScript(
				"log('Please call me back in 5 seconds');\n" +
				"async.callMeMaybe(5000, function(msg) {\n" +
				"    alert(msg);\n" +
				"    log('Whoomp. There it is.');\n" +
				"});\n" +
				"log('async.callMeMaybe() has returned, but wait for it ...');\n"
		);
		// For fun, rotate your device 90 degrees back and forth a few times and see what happens
	}
}
