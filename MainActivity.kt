package ph.gov.deped.region12.soxclmd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ph.gov.deped.region12.soxclmd.ui.AppNav
import ph.gov.deped.region12.soxclmd.ui.AppViewModel
import ph.gov.deped.region12.soxclmd.ui.theme.SOXCLMDTheme
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    private var resumedOnce by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SOXCLMDTheme {
                val vm: AppViewModel = viewModel(factory = AppViewModel.Factory(application))
                val owner = LocalLifecycleOwner.current
                androidx.compose.runtime.DisposableEffect(owner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME && resumedOnce) {
                            vm.refresh() // returning from background -> re-check content
                        }
                        if (event == Lifecycle.Event.ON_RESUME) resumedOnce = true
                    }
                    owner.lifecycle.addObserver(observer)
                    onDispose { owner.lifecycle.removeObserver(observer) }
                }
                AppNav(vm)
            }
        }
    }
}
