
#include <Keyboard.h>

// I needed an extra ground pin, so this pin is set to output low.
const uint8_t GROUND_PIN = 4;

// The number of connected buttons
const uint8_t BUTTON_COUNT = 6;
const uint8_t SWITCH_COUNT = 2;

const uint8_t VIRTUAL_BUTTON_COUNT = BUTTON_COUNT + 2 * SWITCH_COUNT;
bool virtualButtonStates[VIRTUAL_BUTTON_COUNT];

// The minimum time that a key must remain pressed (for debouncing)
const uint32_t MINIMUM_PRESS_MILLIS = 5;

// The time at which each virtual button was pressed, or 0 if the button is not pressed
uint32_t virtualButtonPressTimes[VIRTUAL_BUTTON_COUNT];

// The pin numbers of each button
const uint8_t buttonPins[] = {1, 0, 2, 6, 5, 7};
const uint8_t switchPins[] = {3, 8};

// The keyboard keys corresponding to each button and switch position
const int buttonKeys[] = {'a', 's', 'd', 'j', 'k', 'l'};
const int switchOnKeys[] = {'q', 'u'};
const int switchOffKeys[] = {'w', 'i'};
int virtualButtonKeys[VIRTUAL_BUTTON_COUNT];

// The time at which each switch was toggled, or 0 if the switch is at rest
bool switchWasOn[] = {false, false};

void setup() {
	// Set up pins
	pinMode(GROUND_PIN, OUTPUT);
	digitalWrite(GROUND_PIN, LOW);
	for (uint8_t i = 0; i < BUTTON_COUNT; i++) {
		pinMode(buttonPins[i], INPUT_PULLUP);
	}
	for (uint8_t i = 0; i < SWITCH_COUNT; i++) {
		pinMode(switchPins[i], INPUT_PULLUP);
	}
	pinMode(LED_BUILTIN, OUTPUT);

	// Combine key arrays 
	for (uint8_t i = 0; i < BUTTON_COUNT; i++) {
		virtualButtonKeys[buttonVirtualButtonIndex(i)] = buttonKeys[i];
	}
	for (uint8_t i = 0; i < SWITCH_COUNT; i++) {
		virtualButtonKeys[switchOnVirtualButtonIndex(i)] = switchOnKeys[i];
		virtualButtonKeys[switchOffVirtualButtonIndex(i)] = switchOffKeys[i];
	}

	// Zero arrays
	memset(virtualButtonStates, 0, VIRTUAL_BUTTON_COUNT);
	memset(virtualButtonPressTimes, 0, VIRTUAL_BUTTON_COUNT);

	// Start the keyboard
	Keyboard.begin();
}

void loop() {
	// Loop over all physcial buttons and switches
	for (uint8_t i = 0; i < BUTTON_COUNT; i++) {
		checkButton(i);
	}
	for (uint8_t i = 0; i < SWITCH_COUNT; i++) {
		checkSwitch(i);
	}

	// Pilot light
	digitalWriteFast(LED_BUILTIN, HIGH);
	delayMicroseconds(10);
	digitalWriteFast(LED_BUILTIN, LOW);
	delayMicroseconds(90);
}

void checkButton(uint8_t index) {
	// Read the button input
	bool buttonPressed = !digitalRead(buttonPins[index]);
	virtualButtonStates[buttonVirtualButtonIndex(index)] = buttonPressed;

	// Check the corresponding virtual button
	checkVirtualButton(buttonVirtualButtonIndex(index));
}

// Each switch is represented by two virtual buttons.
// One button or the other is tapped when the switch changes state.
void checkSwitch(uint8_t index) {
	// Read the switch input
	bool switchOn = !digitalRead(switchPins[index]);

	uint8_t virtualOnIndex = switchOnVirtualButtonIndex(index);
	uint8_t virtualOffIndex = switchOffVirtualButtonIndex(index);

	if (switchOn ^ switchWasOn[index]) {
		// Switch just changed state, press one virtual button
		virtualButtonStates[virtualOnIndex] = switchOn;
		// Off button cannot be pressed while the virtual on button is pressed
		virtualButtonStates[virtualOffIndex] = !switchOn &&
			!wasVirtualButtonPressed(virtualOnIndex);
	} else {
		// Switch is in same state, release both virtual buttons
		virtualButtonStates[virtualOnIndex] = false;
		virtualButtonStates[virtualOffIndex] = false;
	}

	// Save the switch state
	switchWasOn[index] = switchOn;

	// Check the corresponding virtual buttons
	checkVirtualButton(virtualOnIndex);
	checkVirtualButton(virtualOffIndex);
}

void checkVirtualButton(uint8_t index) {
	// Read the button input
	bool virtualButtonPressed = virtualButtonStates[index];

	// Do nothing if the virtual button is in the same state as previously
	if (virtualButtonPressed == wasVirtualButtonPressed(index)) {
		return;
	}

	// Button changed state
	if (virtualButtonPressed) {
		// Press
		Keyboard.press(virtualButtonKeys[index]);
		virtualButtonPressTimes[index] = millis();
	} else if (millis() - virtualButtonPressTimes[index] >= MINIMUM_PRESS_MILLIS) {
		// Release after a delay
		Keyboard.release(virtualButtonKeys[index]);
		virtualButtonPressTimes[index] = 0;
	}
}

// Returns true if the button is already pressed.
bool wasVirtualButtonPressed(uint8_t index) {
	return virtualButtonPressTimes[index] != 0;
}

/***************************************************************************
* Helpers to convert button or switch index to virtual button index
***************************************************************************/

constexpr uint8_t buttonVirtualButtonIndex(uint8_t buttonIndex) {
	return buttonIndex;
}

constexpr uint8_t switchOnVirtualButtonIndex(uint8_t switchIndex) {
	return switchIndex + BUTTON_COUNT;
}

constexpr uint8_t switchOffVirtualButtonIndex(uint8_t switchIndex) {
	return switchIndex + BUTTON_COUNT + SWITCH_COUNT;
}
