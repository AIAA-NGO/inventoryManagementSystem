package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.UnitRequest;
import com.example.inventoryManagementSystem.dto.response.UnitResponse;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.Unit;
import com.example.inventoryManagementSystem.repository.UnitRepository;
import com.example.inventoryManagementSystem.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {
    private final UnitRepository unitRepository;
    private final ModelMapper modelMapper;

    @Override
    public UnitResponse createUnit(UnitRequest request) {
        Unit unit = modelMapper.map(request, Unit.class);
        Unit savedUnit = unitRepository.save(unit);
        return modelMapper.map(savedUnit, UnitResponse.class);
    }

    @Override
    public Page<UnitResponse> getAllUnits(int page, int size) {
        return unitRepository.findAll(PageRequest.of(page, size))
                .map(unit -> modelMapper.map(unit, UnitResponse.class));
    }

    @Override
    public UnitResponse getUnitById(Long id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + id));
        return modelMapper.map(unit, UnitResponse.class);
    }

    @Override
    public UnitResponse updateUnit(Long id, UnitRequest request) {
        Unit existingUnit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + id));

        modelMapper.map(request, existingUnit);
        Unit updatedUnit = unitRepository.save(existingUnit);
        return modelMapper.map(updatedUnit, UnitResponse.class);
    }

    @Override
    public void deleteUnit(Long id) {
        if (!unitRepository.existsById(id)) {
            throw new ResourceNotFoundException("Unit not found with id: " + id);
        }
        unitRepository.deleteById(id);
    }

    @Override
    public List<UnitResponse> searchUnits(String query) {
        return unitRepository.findByNameContainingIgnoreCaseOrAbbreviationContainingIgnoreCase(query, query)
                .stream()
                .map(unit -> modelMapper.map(unit, UnitResponse.class))
                .collect(Collectors.toList());
    }
}