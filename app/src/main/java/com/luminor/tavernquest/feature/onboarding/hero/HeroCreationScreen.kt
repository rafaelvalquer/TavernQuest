package com.luminor.tavernquest.feature.onboarding.hero
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luminor.tavernquest.core.designsystem.components.GoldButton

@Composable fun HeroCreationScreen(vm:HeroCreationViewModel=hiltViewModel(),onCreated:()->Unit){val s by vm.ui.collectAsState();LaunchedEffect(s.created){if(s.created)onCreated()};Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){Text("Crie seu herói");HeroPreview(s.heroClass,s.appearance,Modifier.align(Alignment.CenterHorizontally));HeroNameField(s.name,vm::name);HeroAppearanceSelector(s.appearance,vm::appearance);HeroClassSelector(s.heroClass,vm::clazz);GoldButton("Continuar",vm::save,enabled=s.name.isNotBlank()&&!s.saving)}}
