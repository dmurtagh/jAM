// Copyright 2006-2008 Lionel Gueganton
// This file is part of abc4j.
//
// abc4j is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// abc4j is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with abc4j.  If not, see <http://www.gnu.org/licenses/>.
package abc.parser.def;

import scanner.AutomataDefinition;
import scanner.State;
import scanner.Transition;
import abc.parser.AbcTokenType;

/**
 * This scanner extends the capabilities of the default scanner to implement abc
 * tokens scannig. \\ \n start -----------> UNKNOWN ------------> NO LINE BREAK
 * | ^ | \r \n | |----> NO LINE BREAK------------
 **/
public class NoLineBreakDefinition extends AutomataDefinition {

	public NoLineBreakDefinition() {
		State state = new State(AbcTokenType.UNKNOWN, false);
		getStartingState().addTransition(new Transition(state, '\\'));
		State state1 = new State(AbcTokenType.NO_LINE_BREAK, true);
		state.addTransition(new Transition(state1, '\n'));
	}
}
