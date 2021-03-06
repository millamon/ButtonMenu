/*
 * Copyright (c) Tuenti Technologies S.L. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tuenti.buttonmenu;

import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.tuenti.buttonmenu.viewmodel.button.ButtonCommand;
import com.tuenti.buttonmenu.viewmodel.button.ButtonVM;
import com.tuenti.buttonmenu.viewmodel.button.ButtonWithCounterVM;
import com.tuenti.buttonmenu.viewmodel.button.ButtonWithProgressVM;
import com.tuenti.buttonmenu.viewmodel.button.SimpleButtonVM;
import com.tuenti.buttonmenu.viewmodel.buttonmenu.ButtonMenuVM;
import com.tuenti.buttonmenu.viewmodel.buttonmenu.OnButtonCommandExecuted;
import com.tuenti.buttonmenu.viewmodel.buttonmenu.SimpleButtonMenuVM;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Test created to check the correctness of ButtonMenu custom view working with ButtonMenuVM implementations.
 *
 * @author "Pedro Vicente Gómez Sánchez" <pgomez@tuenti.com>
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ButtonMenuTest {

	private static final int ANY_COUNTER_VALUE = 6;
	private static final double DELTA = 0.01;

	private List<ButtonVM> buttonVMs;
	private ButtonMenu buttonMenu;
	private SimpleButtonMenuVM buttonMenuVM;
	private ButtonMenuVM buttonMenuVMWithProgress;
	private ButtonMenuVM buttonMenuVMWithCounter;
	private ButtonWithCounterVM buttonVMWithCounter;
	private ButtonWithProgressVM buttonVMWithProgress;

	@Mock
	private ButtonCommand mockedButtonCommand1;
	@Mock
	private ButtonCommand mockedButtonCommand2;
	@Mock
	private ButtonCommand mockedButtonCommand3;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		buttonMenu = new ButtonMenu(Robolectric.application);

		buttonVMs = new LinkedList<ButtonVM>();
		buttonVMs.add(new SimpleButtonVM(android.R.layout.simple_list_item_1, android.R.id.text1,
				mockedButtonCommand1));
		buttonVMs.add(new SimpleButtonVM(android.R.layout.activity_list_item, android.R.id.icon,
				mockedButtonCommand2));
		buttonMenuVM = new SimpleButtonMenuVM(buttonVMs);

		buttonVMWithCounter = new ButtonWithCounterVM(android.R.layout.simple_list_item_1,
				true, android.R.id.text1, new int[]{android.R.id.text1}, null, ANY_COUNTER_VALUE, android.R.id.text1);
		buttonMenuVMWithCounter = new SimpleButtonMenuVM(buttonVMWithCounter);

		buttonVMWithProgress = new ButtonWithProgressVM(  android.R.layout.simple_expandable_list_item_2, true,
				android.R.id.text1, new int[] { android.R.id.text1, android.R.id.text2 },
				mockedButtonCommand1, android.R.id.text1,android.R.id.text2);
		buttonMenuVMWithProgress = new SimpleButtonMenuVM(buttonVMWithProgress);
	}

	@Test
	public void shouldBeEmptyIfNoButtonMenuVMIsAssociated() {
		buttonMenu.initialize();

		assertEquals(0, buttonMenu.getChildCount());
	}

	@Test
	public void shouldNotInflateButtonVMIfButtonMenuVmIsEmpty() {
		buttonMenu.setButtonMenuVM(new SimpleButtonMenuVM());
		buttonMenu.initialize();

		assertEquals(0, buttonMenu.getChildCount());
	}

	@Test
	public void shouldInflateButtonVMs() {
		initializeButtonMenu();

		assertEquals(2, buttonMenu.getChildCount());
	}

	@Test
	public void shouldExecuteActionCommandIfButtonsAreEnabled() {
		initializeButtonMenu();

		View view = buttonMenu.findViewById(android.R.id.text1);
		view.performClick();

		verify(mockedButtonCommand1).execute();
	}


	@Test
	public void shouldExecuteOnlyOneButtonVMCommand() {
		initializeButtonMenu();

		View view = buttonMenu.findViewById(android.R.id.icon);
		view.performClick();

		verify(mockedButtonCommand2).execute();
		verify(mockedButtonCommand1, never()).execute();
	}

	@Test
	public void shouldDisableButtonVMs() {
		initializeButtonMenu();

		buttonMenuVM.disable();

		assertFalse(buttonMenu.findViewById(android.R.id.text1).isEnabled());
		assertFalse(buttonMenu.findViewById(android.R.id.icon).isEnabled());
	}

	@Test
	public void shouldEnableButtonMenuVM() {
		initializeButtonMenu();

		buttonMenuVM.disable();
		buttonMenuVM.enable();

		assertTrue(buttonMenu.findViewById(android.R.id.text1).isEnabled());
		assertTrue(buttonMenu.findViewById(android.R.id.icon).isEnabled());
	}

	@Test
	public void shouldDisableButtonsOneByOne() {
		initializeButtonMenu();

		for (ButtonVM buttonVM : buttonMenuVM.getButtonVMs()) {
			buttonVM.disable();
		}

		assertFalse(buttonMenu.findViewById(android.R.id.icon).isEnabled());
		assertFalse(buttonMenu.findViewById(android.R.id.text1).isEnabled());
	}

	@Test
	public void shouldNotifyOnButtonCommandExecuted() {
		initializeButtonMenu();
		OnButtonCommandExecuted mockedOnButtonCommandExecuted = mock(OnButtonCommandExecuted.class);
		buttonMenu.setOnButtonCommandExecutedListener(mockedOnButtonCommandExecuted);

		buttonMenu.findViewById(android.R.id.text1).performClick();

		verify(mockedOnButtonCommandExecuted).onActionCommandExecuted();
	}

	@Test
	public void shouldReleaseButtonRemovingButtons() {
		initializeButtonMenu();

		buttonMenu.release();

		assertEquals(0, buttonMenu.getChildCount());
	}

	@Test
	public void everyChildShouldHaveTheSameWeightWithCorrectSum() {
		initializeButtonMenu();

		assertEveryChildHasTheSameWeightWithCorrectSum();
	}

	@Test
	public void everyChildShouldHaveTheSameWeightWithCorrectSumAfterAddingButton() {
		initializeButtonMenu();

		buttonMenuVM.addItem(new SimpleButtonVM(android.R.layout.simple_list_item_2,
				android.R.id.text2, mockedButtonCommand3));

		assertEveryChildHasTheSameWeightWithCorrectSum();
	}

	@Test
	public void everyChildShouldHaveTheSameWeightWithCorrectSumAfterRemovingButton() {
		initializeButtonMenu();

		buttonMenuVM.removeItem(buttonVMs.get(0));

		assertEveryChildHasTheSameWeightWithCorrectSum();
	}

	private void assertEveryChildHasTheSameWeightWithCorrectSum() {
		float weightSum = 0;
		float commonWeight = -1;
		for (int i = 0; i < buttonMenu.getChildCount(); i++) {
			View button = buttonMenu.getChildAt(i);
			float buttonWeight = ((LayoutParams) button.getLayoutParams()).weight;
			weightSum += buttonWeight;
			if (commonWeight == -1) {
				commonWeight = buttonWeight;
			} else {
				assertEquals(commonWeight, buttonWeight, DELTA);
			}
		}
		assertEquals(ButtonMenu.WEIGHT_SUM, weightSum, DELTA);
	}

	@Test
	public void shouldUpdateCounterValue() {
		initializeButtonMenuWithButtonMenuVM(buttonMenuVMWithCounter);

		buttonVMWithCounter.setCounterValue(ANY_COUNTER_VALUE);

		TextView counter = (TextView) buttonMenu.findViewById(android.R.id.text1);
		assertEquals(ANY_COUNTER_VALUE, Integer.parseInt((String) counter.getText()));
	}

	private void initializeButtonMenu() {
		initializeButtonMenuWithButtonMenuVM(buttonMenuVM);
	}

	@Test
	public void shouldUpdateProgressValue() {
		initializeButtonMenuWithButtonMenuVM(buttonMenuVMWithProgress);

		buttonVMWithProgress.showLoading();

		assertTrue(buttonVMWithProgress.isLoading());
	}

	@Test
	public void shouldUpdateProgressCloseValue() {
		initializeButtonMenuWithButtonMenuVM(buttonMenuVMWithProgress);

		buttonVMWithProgress.closeLoading();

		assertFalse(buttonVMWithProgress.isLoading());
	}

	private void initializeButtonMenuWithButtonMenuVM(ButtonMenuVM buttonMenuVM) {
		buttonMenu.setButtonMenuVM(buttonMenuVM);
		buttonMenu.initialize();
	}

}
