const TOGGLE = 'ml/TOGGLE';
const LOAD_FROM_LS = 'ml/LOAD_FROM_LS';

const LS_KEY = 'ml_toggled';

const initialState = {
	toggle: false
};

export default function reducer	(state = initialState, action = {}) {
	switch (action.type) {
		case LOAD_FROM_LS:
			return {
				toggle: action.toggled
			};
		case TOGGLE:
			let toggled = !state.toggle;
			saveToLS(toggled);
			return {
				toggled: toggled
			};
		default:
			return state;
	}
}

// return toggle action
export function toggle() {
	return {
		type: TOGGLE
	};
}

export function saveToLS(toggled) {
	window.ls.setItem(LS_KEY, toggled);
}

export function loadFromLS() {
	let toggled = false;
	if (!__SERVER__) {
		//!server side
		let json = ls.getItem(LS_KEY);
		if (json) {
			try {
				toggled = !!JSON.parse(json);
			} catch (e) {
				toggled = false;
			}
		}
	}
	return {
		type: LOAD_FROM_LS,
		toggled: toggled
	};
}