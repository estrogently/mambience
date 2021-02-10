/*
 * Copyright (c) 2021 André Schweiger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.andre111.mambience.config;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.andre111.mambience.MALogger;
import me.andre111.mambience.condition.Condition;
import me.andre111.mambience.condition.Parser;
import me.andre111.mambience.sound.Sound;

public class ConfigUtil {
	public static List<Condition> loadConditions(MALogger logger, JsonArray array) {
		List<Condition> conditions = new ArrayList<>();
		for(int i=0; i<array.size(); i++) {
			//TODO: remove: ignore toggles
			if(array.get(i).getAsJsonObject().get("condition").getAsString().equals("TOGGLE")) continue;
			
			Condition condition = loadCondition(logger, array.get(i).getAsJsonObject());
			if(condition != null) {
				conditions.add(condition);
			} else {
				logger.log("Warning: Ignored unknown condition: "+array.get(i));
			}
		}
		return conditions;
	}
	
	private static Condition loadCondition(MALogger logger, JsonObject obj) {
		String name = getString(obj, "condition", "");
		String stringValue = getString(obj, "stringValue", "");
		float floatValue = getFloat(obj, "floatValue", 0);
		return Parser.parse(name, stringValue, floatValue);
	}
	
	public static Sound[] loadSounds(JsonElement element, float volumeMultiplier) {
		if(element == null) return new Sound[] {};
		// shortcut: use just a string for the sound name
		if(element.isJsonPrimitive()) {
			String name = element.getAsString();
			if(!name.contains(":")) name = "mambience:" + name;
			return new Sound[] { new Sound(name, 1, 1, 1, 1, 0, 1) };
		}
		// object: single sound object
		if(element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			
			String name = object.get("name").getAsString();
			if(!name.contains(":")) name = "mambience:" + name;
			float volumeMin = object.has("volumeMin") ? object.get("volumeMin").getAsFloat() / 100 : (object.has("vol") ? object.get("vol").getAsFloat() / 100 : 1);
			float volumeMax = object.has("volumeMax") ? object.get("volumeMax").getAsFloat() / 100 : (object.has("vol") ? object.get("vol").getAsFloat() / 100 : 1);
			float pitchMin = object.has("pitchMin") ? object.get("pitchMin").getAsFloat() / 100 : (object.has("pitch") ? object.get("pitch").getAsFloat() / 100 : 1);
			float pitchMax = object.has("pitchMax") ? object.get("pitchMax").getAsFloat() / 100 : (object.has("pitch") ? object.get("pitch").getAsFloat() / 100 : 1);
			int delay = object.has("delay") ? object.get("delay").getAsInt() : 0;
			double probability = object.has("probability") ? object.get("probability").getAsDouble() : 1;
			
			volumeMin *= volumeMultiplier;
			volumeMax *= volumeMultiplier;
			
			return new Sound[] { new Sound(name, volumeMin, volumeMax, pitchMin, pitchMax, delay, probability ) };
		}
		// array: list of sounds (with full recursive parsing)
		if(element.isJsonArray()) {
			JsonArray array = element.getAsJsonArray();
			
			List<Sound> sounds = new ArrayList<>();
			for(int i=0; i<array.size(); i++) {
				for(Sound sound : loadSounds(array.get(i), volumeMultiplier)) {
					sounds.add(sound);
				}
			}
			return sounds.toArray(new Sound[0]);
		}
		throw new RuntimeException("Unsupported sound format: "+element);
	}
	
	public static String getString(JsonObject obj, String memberName, String defaultValue) {
		return obj.has(memberName) ? obj.get(memberName).getAsString() : defaultValue;
	}
	public static double getDouble(JsonObject obj, String memberName, float defaultValue) {
		return obj.has(memberName) ? obj.get(memberName).getAsDouble() : defaultValue;
	}
	public static float getFloat(JsonObject obj, String memberName, float defaultValue) {
		return obj.has(memberName) ? obj.get(memberName).getAsFloat() : defaultValue;
	}
	public static int getInt(JsonObject obj, String memberName, int defaultValue) {
		return obj.has(memberName) ? obj.get(memberName).getAsInt() : defaultValue;
	}
	
	public static String[] getStringArray(JsonObject obj, String memberName, String[] defaultValue) {
		if(obj.has(memberName) && obj.get(memberName).isJsonArray()) {
			JsonArray jsonArray = obj.get(memberName).getAsJsonArray();
			String[] stringArray = new String[jsonArray.size()];
			for(int i=0; i<jsonArray.size(); i++) {
				stringArray[i] = jsonArray.get(i).getAsString();
			}
			return stringArray;
		}
		return defaultValue;
	}
}
