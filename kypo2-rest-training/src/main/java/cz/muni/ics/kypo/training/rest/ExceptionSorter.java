package cz.muni.ics.kypo.training.rest;

import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.rest.exceptions.*;

public class ExceptionSorter {

	public static RuntimeException throwException(RuntimeException ex) {
		switch (((ServiceLayerException) ex.getCause()).getCode()) {
			case WRONG_LEVEL_TYPE:
				return new BadRequestException(ex.getCause().getClass().getSimpleName() + " : " + ex.getCause().getLocalizedMessage());
			case RESOURCE_NOT_FOUND:
				return new ResourceNotFoundException(ex.getCause().getClass().getSimpleName() + " : " + ex.getCause().getLocalizedMessage());
			case NO_NEXT_LEVEL:
				return new ResourceNotFoundException(ex.getCause().getClass().getSimpleName() + " : " + ex.getCause().getLocalizedMessage());
			case UNEXPECTED_ERROR:
				return new InternalServerErrorException(ex.getCause().getClass().getSimpleName() + " : " + ex.getCause().getLocalizedMessage());
			case RESOURCE_CONFLICT:
				return new ConflictException(ex.getCause().getClass().getSimpleName() + " : " + ex.getCause().getLocalizedMessage());
			case NO_AVAILABLE_SANDBOX:
			default:
				return new ServiceUnavailableException(ex.getCause().getClass().getSimpleName() + " : " + ex.getCause().getLocalizedMessage());
		}
	}

}

