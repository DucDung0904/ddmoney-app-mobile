package com.dung.ddmoney.ui.components

import com.dung.ddmoney.category.DefaultCategorySeed
import com.dung.ddmoney.category.DefaultCategorySpec
import com.dung.ddmoney.ui.dashboard.model.Category

fun List<Category>.withResolvedCategoryHierarchy(): List<Category> {
    if (isEmpty()) return this

    fun specFor(category: Category): DefaultCategorySpec? =
        DefaultCategorySeed.findById(category.id)
            ?: if (category.isDefault || category.userId == null) {
                DefaultCategorySeed.findByName(category.name)
            } else {
                null
            }

    fun existingParentId(parentSeedId: Long): String? {
        val parentSpec = DefaultCategorySeed.findById(parentSeedId.toString()) ?: return null
        return firstOrNull { it.id == parentSeedId.toString() }?.id
            ?: firstOrNull { candidate ->
                candidate.parentId == null &&
                    (candidate.isDefault || candidate.userId == null) &&
                    DefaultCategorySeed.normalizeName(candidate.name) ==
                    DefaultCategorySeed.normalizeName(parentSpec.name)
            }?.id
    }

    val resolved =
        map { category ->
            val spec = specFor(category)
            val isSystemDefault = category.isDefault || (category.userId == null && spec != null)
            val inferredParentId =
                category.parentId ?: spec?.parentId?.let { seedParentId ->
                    existingParentId(seedParentId)
                }

            ResolvedCategory(
                category =
                    category.copy(
                        isDefault = isSystemDefault,
                        isEditable = if (isSystemDefault) false else category.isEditable,
                        isDeletable = if (isSystemDefault) false else category.isDeletable,
                        parentId = inferredParentId,
                        sortOrder =
                            if (isSystemDefault && spec != null) {
                                spec.sortOrder
                            } else {
                                category.sortOrder
                            }
                    ),
                seedSpec = spec
            )
        }

    val preferredDefaultIdsBySeed =
        resolved
            .filter { it.category.isDefault && it.seedSpec != null }
            .groupBy { it.seedSpec!!.id }
            .mapValues { (_, group) ->
                group.minWith(
                    compareBy<ResolvedCategory>(
                        { if (it.category.id == it.seedSpec!!.id.toString()) 0 else 1 },
                        { if (it.category.userId == null) 0 else 1 },
                        { it.category.sortOrder },
                        { it.category.name }
                    )
                ).category.id
            }

    return resolved
        .filter { item ->
            val seedId = item.seedSpec?.id
            seedId == null ||
                !item.category.isDefault ||
                preferredDefaultIdsBySeed[seedId] == item.category.id
        }
        .map { it.category }
        .sortedWith(compareBy({ it.sortOrder }, { it.name }))
}

private data class ResolvedCategory(
    val category: Category,
    val seedSpec: DefaultCategorySpec?
)
