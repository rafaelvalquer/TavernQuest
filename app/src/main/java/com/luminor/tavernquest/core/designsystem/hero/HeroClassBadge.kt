package com.luminor.tavernquest.core.designsystem.hero
import androidx.compose.material3.*;import androidx.compose.runtime.Composable;import com.luminor.tavernquest.domain.model.HeroClass
@Composable fun HeroClassBadge(c:HeroClass){AssistChip(onClick={},label={Text(c.title)})}
