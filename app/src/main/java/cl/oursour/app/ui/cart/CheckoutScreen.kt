package cl.oursour.app.ui.cart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.oursour.app.data.remote.dto.PaymentRequest
import cl.oursour.app.ui.common.toClp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(vm: CheckoutViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    val ui by vm.ui.collectAsState()
    var street by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var cardNumber by rememberSaveable { mutableStateOf("") }
    var cardHolder by rememberSaveable { mutableStateOf("") }
    var expiry by rememberSaveable { mutableStateOf("") }
    var cvv by rememberSaveable { mutableStateOf("") }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(when (ui.step) { 1 -> "Dirección de envío"; 2 -> "Pago"; else -> "Pedido confirmado" }) },
            navigationIcon = {
                if (ui.step < 3) IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        )
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            when (ui.step) {
                1 -> {
                    OutlinedTextField(street, { street = it }, label = { Text("Calle y número") },
                        isError = "street" in ui.fieldErrors,
                        supportingText = { ui.fieldErrors["street"]?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(city, { city = it }, label = { Text("Ciudad") },
                        isError = "city" in ui.fieldErrors,
                        supportingText = { ui.fieldErrors["city"]?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth())
                    ErrorText(ui.error)
                    Button(onClick = { vm.confirmAddress(street.trim(), city.trim()) },
                        enabled = !ui.loading && street.isNotBlank() && city.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("Continuar al pago") }
                }
                2 -> {
                    ui.order?.let { Text("Total a pagar: ${it.total.toClp()}", style = MaterialTheme.typography.titleLarge) }
                    OutlinedTextField(cardNumber, { cardNumber = it.filter(Char::isDigit).take(16) },
                        label = { Text("Número de tarjeta (16 dígitos)") }, singleLine = true,
                        isError = "cardNumber" in ui.fieldErrors,
                        supportingText = { ui.fieldErrors["cardNumber"]?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(cardHolder, { cardHolder = it }, label = { Text("Titular") }, singleLine = true,
                        isError = "cardHolder" in ui.fieldErrors,
                        supportingText = { ui.fieldErrors["cardHolder"]?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(expiry, { expiry = it.take(5) }, label = { Text("MM/AA") }, singleLine = true,
                            isError = "expiry" in ui.fieldErrors,
                            modifier = Modifier.weight(1f))
                        OutlinedTextField(cvv, { cvv = it.filter(Char::isDigit).take(4) }, label = { Text("CVV") },
                            singleLine = true, isError = "cvv" in ui.fieldErrors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f))
                    }
                    ErrorText(ui.error)
                    Button(onClick = { vm.pay(PaymentRequest(cardNumber, cardHolder.trim(), expiry.trim(), cvv)) },
                        enabled = !ui.loading,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("Pagar") }
                }
                else -> {
                    val o = ui.order
                    Text("¡Gracias por tu compra!", style = MaterialTheme.typography.headlineSmall)
                    if (o != null) {
                        Text("Pedido #${o.id} · ${o.status}")
                        Text("Total: ${o.total.toClp()}")
                        o.paymentReference?.let { Text("Referencia de pago: $it") }
                    }
                    Button(onClick = onDone, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("Volver al inicio") }
                }
            }
        }
    }
}

@Composable
private fun ErrorText(error: String?) {
    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
}
