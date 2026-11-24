package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.AddressEntity
import com.example.data.model.OrderEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.PaleGoldContainer
import com.example.ui.theme.SubtleGold

@Composable
fun AccountScreen(
    activeUser: UserEntity?,
    adminCount: Int,
    savedAddresses: List<AddressEntity>,
    orders: List<OrderEntity>,
    authError: String?,
    onClearError: () -> Unit,
    onLogin: (emailOrMobile: String, password: String) -> Unit,
    onSignUp: (name: String, email: String, mobile: String, password: String, asInitialAdmin: Boolean) -> Unit,
    onResetPassword: (email: String, mobile: String, newPassword: String, onSuccess: () -> Unit) -> Unit,
    onLogout: () -> Unit,
    onUpdateProfile: (name: String, mobile: String) -> Unit,
    onSaveAddress: (AddressEntity) -> Unit,
    onDeleteAddress: (String) -> Unit,
    onOpenOrders: () -> Unit,
    onOpenWholesale: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAdminDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeUser == null) {
        AuthContent(
            adminCount = adminCount,
            authError = authError,
            onClearError = onClearError,
            onLogin = onLogin,
            onSignUp = onSignUp,
            onResetPassword = onResetPassword,
            modifier = modifier
        )
        return
    }

    var showEditProfile by remember { mutableStateOf(false) }
    var showAddAddress by remember { mutableStateOf(false) }
    val isAdmin = activeUser.role == UserRole.ADMIN.key

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Column {
                                Text(activeUser.fullName, style = MaterialTheme.typography.titleLarge)
                                Text(activeUser.email, style = MaterialTheme.typography.bodySmall)
                                Text("Mobile: ${activeUser.mobile}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Surface(
                            color = PaleGoldContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isAdmin) "STORE ADMIN" else "CUSTOMER",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showEditProfile = true }) {
                            Text("Edit Profile")
                        }
                        OutlinedButton(
                            onClick = onLogout,
                            modifier = Modifier.testTag("account_logout_button")
                        ) {
                            Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Logout")
                        }
                    }
                }
            }
        }

        if (isAdmin) {
            item {
                Button(
                    onClick = onOpenAdminDashboard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("open_admin_dashboard_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SubtleGold,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(Icons.Filled.AdminPanelSettings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Store Admin Dashboard")
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    AccountShortcutRow(
                        icon = Icons.Filled.ReceiptLong,
                        title = "Order History & Live Tracking",
                        subtitle = "${orders.size} order(s) placed",
                        onClick = onOpenOrders
                    )
                    HorizontalDivider()
                    AccountShortcutRow(
                        icon = Icons.Filled.Business,
                        title = "Wholesale & Bulk Orders",
                        subtitle = "Retailer enquiries & custom quotations",
                        onClick = onOpenWholesale
                    )
                    HorizontalDivider()
                    AccountShortcutRow(
                        icon = Icons.Filled.Notifications,
                        title = "Notifications",
                        subtitle = "Order, payment & delivery status alerts",
                        onClick = onOpenNotifications
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Saved Delivery Addresses (${savedAddresses.size})", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { showAddAddress = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Add Address")
                }
            }
        }

        if (savedAddresses.isEmpty()) {
            item {
                Text(
                    text = "No saved delivery addresses yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(savedAddresses, key = { it.id }) { addr ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "${addr.label}${if (addr.isDefault) " (Default)" else ""}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text("${addr.fullName} • ${addr.mobile}", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "${addr.houseNumber}, ${addr.building}, ${addr.street}, ${addr.area}, ${addr.city}, ${addr.state} - ${addr.pinCode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onDeleteAddress(addr.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete address", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showEditProfile) {
        var name by remember { mutableStateOf(activeUser.fullName) }
        var mobile by remember { mutableStateOf(activeUser.mobile) }
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text("Update Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, singleLine = true)
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it.filter { c -> c.isDigit() }.take(10) },
                        label = { Text("10-Digit Mobile") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateProfile(name, mobile)
                    showEditProfile = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfile = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddAddress) {
        AddAddressDialog(
            defaultName = activeUser.fullName,
            defaultMobile = activeUser.mobile,
            onDismiss = { showAddAddress = false },
            onSave = { addr ->
                onSaveAddress(addr.copy(userId = activeUser.id))
                showAddAddress = false
            }
        )
    }
}

@Composable
private fun AccountShortcutRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AuthContent(
    adminCount: Int,
    authError: String?,
    onClearError: () -> Unit,
    onLogin: (String, String) -> Unit,
    onSignUp: (String, String, String, String, Boolean) -> Unit,
    onResetPassword: (String, String, String, () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Login, 1=SignUp, 2=Forgot Password
    var identifier by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var claimAdmin by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Customer & Store Account", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Sign in to sync your cart, save delivery addresses, track orders, and submit verified reviews.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; onClearError() },
                    text = { Text("Login") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; onClearError() },
                    text = { Text("Sign Up") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2; onClearError() },
                    text = { Text("Reset") }
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            OutlinedTextField(
                                value = identifier,
                                onValueChange = { identifier = it },
                                label = { Text("Email or 10-Digit Mobile") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_identifier_input")
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_input")
                            )
                            Button(
                                onClick = { onLogin(identifier, password) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("login_submit_button")
                            ) {
                                Text("Login")
                            }
                        }

                        1 -> {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name *") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_name_input")
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_email_input")
                            )
                            OutlinedTextField(
                                value = mobile,
                                onValueChange = { mobile = it.filter { c -> c.isDigit() }.take(10) },
                                label = { Text("10-Digit Mobile Number *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_mobile_input")
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password (min 6 characters) *") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_password_input")
                            )

                            if (adminCount == 0) {
                                Surface(
                                    color = PaleGoldContainer,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .clickable { claimAdmin = !claimAdmin },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = claimAdmin,
                                            onCheckedChange = { claimAdmin = it },
                                            modifier = Modifier.testTag("signup_claim_admin_checkbox")
                                        )
                                        Column {
                                            Text("First-Run Store Admin Setup", style = MaterialTheme.typography.titleSmall)
                                            Text(
                                                "No Store Admin exists yet. Check this to register as the Store Administrator.",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = { onSignUp(fullName, email, mobile, password, claimAdmin) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("signup_submit_button")
                            ) {
                                Text(if (claimAdmin && adminCount == 0) "Create Store Admin Account" else "Create Customer Account")
                            }
                        }

                        else -> {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Registered Email *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = mobile,
                                onValueChange = { mobile = it.filter { c -> c.isDigit() }.take(10) },
                                label = { Text("Registered 10-Digit Mobile *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("New Password *") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    onResetPassword(email, mobile, password) {
                                        selectedTab = 0
                                        password = ""
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("Reset Password")
                            }
                        }
                    }

                    authError?.let { err ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddAddressDialog(
    defaultName: String,
    defaultMobile: String,
    onDismiss: () -> Unit,
    onSave: (AddressEntity) -> Unit
) {
    var label by remember { mutableStateOf("Home") }
    var fullName by remember { mutableStateOf(defaultName) }
    var mobile by remember { mutableStateOf(defaultMobile) }
    var house by remember { mutableStateOf("") }
    var building by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("Maharashtra") }
    var pinCode by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Delivery Address") },
        text = {
            LazyColumn(
                modifier = Modifier.height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label (Home/Work)") }, singleLine = true) }
                item { OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, singleLine = true) }
                item { OutlinedTextField(value = mobile, onValueChange = { mobile = it.filter { c -> c.isDigit() }.take(10) }, label = { Text("Mobile") }, singleLine = true) }
                item { OutlinedTextField(value = house, onValueChange = { house = it }, label = { Text("House No *") }, singleLine = true) }
                item { OutlinedTextField(value = building, onValueChange = { building = it }, label = { Text("Building") }, singleLine = true) }
                item { OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Street *") }, singleLine = true) }
                item { OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Area *") }, singleLine = true) }
                item { OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City *") }, singleLine = true) }
                item { OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State *") }, singleLine = true) }
                item { OutlinedTextField(value = pinCode, onValueChange = { pinCode = it.filter { c -> c.isDigit() }.take(6) }, label = { Text("PIN Code *") }, singleLine = true) }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                        Text("Set as default address")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = house.isNotBlank() && street.isNotBlank() && city.isNotBlank() && pinCode.length == 6,
                onClick = {
                    onSave(
                        AddressEntity(
                            id = "",
                            userId = "",
                            label = label.ifBlank { "Home" },
                            fullName = fullName,
                            mobile = mobile,
                            houseNumber = house,
                            building = building,
                            street = street,
                            area = area,
                            city = city,
                            state = state,
                            pinCode = pinCode,
                            isDefault = isDefault
                        )
                    )
                }
            ) { Text("Save Address") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
