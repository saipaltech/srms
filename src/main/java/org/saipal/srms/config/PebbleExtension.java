package org.saipal.srms.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PebbleExtension {//extends AbstractExtension {


//	@Override
//	public Map<String, Function> getFunctions() {
//		Map<String, Function> allFunctions = new HashMap<>();
//		allFunctions.put("getSetting", getSetting);
//		return allFunctions;
//	}
//
//	public Function getSetting = new Function() {
//		@Override
//		public List<String> getArgumentNames() {
//			return List.of("key");
//		}
//
//		@Override
//		public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context,
//				int lineNumber) {
//			String key = args.get("key") + "";
//			if (!key.isBlank()) {
//				return "";
//			}
//			return "";
//		}
//	};

}
